package net.geeksempire.chat.vicinity.Util.RoomSqLiteDatabase.UserInformation

import androidx.room.*

@Dao
interface WidgetDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewWidgetData(vararg arrayOfWidgetDataModels: WidgetDataModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateWidgetData(vararg arrayOfWidgetDataModels: WidgetDataModel)

    @Delete
    fun delete(widgetDataModel: WidgetDataModel)

    @Query("SELECT * FROM WidgetData ORDER BY AppName ASC")
    fun getAllWidgetData(): List<WidgetDataModel>

    @Query("SELECT * FROM WidgetData WHERE WidgetId IN (:WidgetId)")
    fun loadWidgetById(WidgetId: Int): WidgetDataModel
}