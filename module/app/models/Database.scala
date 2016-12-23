package models

import org.squeryl.{Schema, Table}
import org.squeryl.PrimitiveTypeMode._

object Database extends Schema {
  val productsTable: Table[Product] =
    table[Product]("products")
  val stockItemsTable: Table[StockItem] =
    table[StockItem]("stock_items")
  val warehousesTable: Table[Warehouse] =
    table[Warehouse]("warehouses")

  on(productsTable) { p => declare {
    p.id is(autoIncremented)
  }}

  on(stockItemsTable) { s => declare {
    s.id is(autoIncremented)
  }}

  on(warehousesTable) { w => declare {
    w.id is(autoIncremented)
  }}

  val productToStockItems =
    oneToManyRelation(productsTable, stockItemsTable).
      via((p,s) => p.id === s.productId)

  val warehouseToStockItems =
    oneToManyRelation(warehousesTable, stockItemsTable).
      via((w,s) => w.id === s.warehouseId)
}
