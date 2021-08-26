package com.androiddevs.ktornoteapp.data.other

import kotlinx.coroutines.flow.*

/*
    Function to handle and implement the cache mechanism
    ResultType -> Everything that comes from the database (can be the database model)
    RequestType -> Everything that comes from the api (can be the api model)
 */

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(Resource.loading(null))
    //get cached data from database
    val data = query().first()

    // verify if we want to fetch data from api or not
    val flow = if (shouldFetch(data)) {
        emit(Resource.loading(data))

        try {
            //get data from network
            val fetchedResult = fetch()
            //save this data into database
            saveFetchResult(fetchedResult)
            query().map { Resource.success(it) }
        }catch (t: Throwable) {
            onFetchFailed(t)
            query().map { Resource.error("Couldn´t reach server. It might be down",it) }
        }
    } else {
        query().map { Resource.success(it) }
    }
    emitAll(flow)
}