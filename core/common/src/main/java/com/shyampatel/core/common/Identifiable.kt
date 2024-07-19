package com.shyampatel.core.common

@Deprecated("")
interface Identifiable<T : Identifiable<T, RawIdentifier>, RawIdentifier> {
    val id: Identifier<T, RawIdentifier>
}
@JvmInline
/**
 * Todo check hashcode and tostring
 */
@Deprecated("")
value class Identifier<T: Identifiable<T, RawIdentifier>, RawIdentifier>(val rawValue: RawIdentifier)

/**
 * Sample usage
 * TODO delete
 */
data class SampleUser(
    override val id: Identifier<SampleUser, String>,
    val name: String
) : Identifiable<SampleUser, String>

