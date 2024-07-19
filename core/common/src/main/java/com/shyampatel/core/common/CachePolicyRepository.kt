package com.shyampatel.core.common
import com.shyampatel.core.common.CachePolicy.Type.ALWAYS
import com.shyampatel.core.common.CachePolicy.Type.CLEAR
import com.shyampatel.core.common.CachePolicy.Type.EXPIRES
import com.shyampatel.core.common.CachePolicy.Type.NEVER
import com.shyampatel.core.common.CachePolicy.Type.REFRESH

@Deprecated("Experimental. Will be deleted")
abstract class CachePolicyRepository<T>(
    private val localDataSource: LocalDataSource<String, CacheEntry<T>>,
    private val remoteDataSource: RemoteDataSource<T>
) {

    fun fetch(url: String, cachePolicy: CachePolicy): T? {
        return when (cachePolicy.type) {
            NEVER -> remoteDataSource.fetch(url)
            ALWAYS -> {
                localDataSource.get(url)?.value ?: fetchAndCache(url)
            }
            CLEAR -> {
                localDataSource.get(url)?.value.also {
                    localDataSource.remove(url)
                }
            }
            REFRESH -> fetchAndCache(url)
            EXPIRES -> {
                localDataSource.get(url)?.let { 
                    if( (it.createdAt + cachePolicy.expires) > System.currentTimeMillis()) { 
                        it.value
                    } else { 
                        fetchAndCache(url)
                    }
                } ?: fetchAndCache(url)
            }
            else -> null
        }
    }
    
    private fun fetchAndCache(url:String): T { 
        return remoteDataSource.fetch(url).also {
            localDataSource.set(url, CacheEntry(key = url, value = it))
        }
    }
}

data class CacheEntry<T>(
    val key: String,
    val value: T,
    val createdAt: Long = System.currentTimeMillis()
)

interface LocalDataSource<in Key : Any, T> {
    fun get(key: Key): T?
    fun set(key: Key, value: T)
    fun remove(key: Key)
    fun clear()
}

interface RemoteDataSource<T> {
    fun fetch(url: String): T
}

