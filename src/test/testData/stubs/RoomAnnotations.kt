package androidx.room

import kotlin.reflect.KClass

annotation class Database(
    val entities: Array<KClass<*>> = [],
    val views: Array<KClass<*>> = [],
    val version: Int = 1,
    val exportSchema: Boolean = true
)

annotation class Entity(
    val tableName: String = "",
    val primaryKeys: Array<String> = [],
    val foreignKeys: Array<ForeignKey> = [],
    val indices: Array<Index> = [],
    val inheritSuperIndices: Boolean = false
)

annotation class Dao

annotation class Query(val value: String)

annotation class Insert(val onConflict: Int = OnConflictStrategy.ABORT)

annotation class Update(val onConflict: Int = OnConflictStrategy.ABORT)

annotation class Delete

annotation class Upsert

annotation class RawQuery

annotation class ColumnInfo(
    val name: String = "",
    val typeAffinity: Int = UNDEFINED,
    val defaultValue: String = VALUE_UNSPECIFIED,
    val collate: Int = UNSPECIFIED
) {
    companion object {
        const val UNDEFINED = 0
        const val UNSPECIFIED = 0
        const val VALUE_UNSPECIFIED = "[value-unspecified]"
    }
}

annotation class PrimaryKey(val autoGenerate: Boolean = false)

annotation class ForeignKey(
    val entity: KClass<*>,
    val parentColumns: Array<String>,
    val childColumns: Array<String>,
    val onDelete: Int = NO_ACTION,
    val onUpdate: Int = NO_ACTION
) {
    companion object {
        const val NO_ACTION = 1
        const val RESTRICT = 2
        const val SET_NULL = 3
        const val SET_DEFAULT = 4
        const val CASCADE = 5
    }
}

annotation class Index(
    val value: Array<String>,
    val name: String = "",
    val unique: Boolean = false
)

annotation class Embedded(val prefix: String = "")

annotation class Ignore

annotation class Relation(
    val parentColumn: String = "",
    val entityColumn: String = "",
    val entity: KClass<*> = Any::class
)

annotation class TypeConverter

annotation class TypeConverters(vararg val value: KClass<*>)

annotation class DatabaseView(val value: String, val viewName: String = "")

annotation class Transaction

object OnConflictStrategy {
    const val NONE = 0
    const val REPLACE = 1
    const val ROLLBACK = 2
    const val ABORT = 3
    const val FAIL = 4
    const val IGNORE = 5
}

abstract class RoomDatabase
