package models

import anorm.{ResultSetParser, RowParser, SQL, SqlQuery}
import org.squeryl.{KeyedEntity, Query, Table}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.{ManyToOne, OneToMany}
import play.api.Play.current
import play.api.db.DB

case class Product (
  id: Long,
  ean: Long,
  name: String,
  description: String
) extends KeyedEntity[Long] {
  lazy val stockItems: OneToMany[StockItem] =
    Database.productToStockItems.left(this)
}

object Product {
  var products = Set(
    Product(1L, 5010255079763L, "Paperclips Large",
      "Large Plain Pack of 1000"),
    Product(2L, 5018206244666L, "Giant Paperclips",
      "Giant Plain 51mm 100 pack"),
    Product(3L, 5018306332812L, "Paperclip Giant Plain",
      "Giant Plain Pack of 10000"),
    Product(4L, 5018306312913L, "No Tear Paper Clip",
      "No Tear Extra Large Pack of 1000"),
    Product(5L, 5018206244611L, "Zebra Paperclips",
      "Zebra Length 28mm Assorted 150 Pack")
  )

  def findAll = products.toList.sortBy(_.ean)

  def findByEan(ean: Long) = products.find(_.ean == ean)

  def add(product: Product) {
    products = products + product
  }

  val sql: SqlQuery = SQL("select * from products order by name asc")

  def getAll: List[Product] = DB.withConnection {
    implicit connection =>
      sql().map(row =>
        Product(row[Long]("id"), row[Long]("ean"),
          row[String]("name"), row[String]("description"))
      ).toList
  }

  def getWithPatterns: List[Product] = DB.withConnection{
    implicit connection =>
      import anorm.Row
      sql().collect {
        case Row(Some(id: Long), Some(ean: Long),
        Some(name: String), Some(description: String)) =>
          Product(id, ean, name, description)
      }.toList
  }

  val productParser: RowParser[Product] = {
    import anorm.~
    import anorm.SqlParser._

    long("id")~
    long("ean")~
    str("name")~
    str("description") map {
      case id ~ ean ~ name ~ description =>
        Product(id, ean, name, description)
    }
  }
  val productsParser: ResultSetParser[List[Product]] = {
    productParser *
  }
  def getAllWithParser: List[Product] = DB.withConnection {
    implicit connection =>
    sql.as(productsParser)
  }

  def productStockItemParser: RowParser[(Product, StockItem)] = {
    import anorm.SqlParser._
    productParser ~ StockItem.stockItemParser map (flatten)
  }

  def getAllProductsWithStockItems: Map[Product, List[StockItem]] = {
    DB.withConnection { implicit connection =>
      val sql = SQL("select p.*, s.* " +
      "from products p" +
      "inner join stock_items s on (p.id = s.product_id)")
      val results: List[(Product, StockItem)] =
        sql.as(productStockItemParser *)
      results.groupBy(_._1).mapValues {_.map {_._2}}
    }
  }

  def insert(product: Product): Boolean =
    DB.withConnection{ implicit connection =>
      val addedRows = SQL(
        """insert into products
          |values({id}, {ean}, {name}, {description}""").on(
          "id" -> product.id,
          "ean" -> product.ean,
          "name" -> product.name,
          "description" -> product.description
        ).executeUpdate()
      addedRows == 1
    }

  def update(product: Product): Boolean =
    DB.withConnection { implicit connection =>
      val updateRows = SQL(
        s"""update products
          set name = {name},
          ean = {ean},
          description = {description}
          where
          id = {id}
        """).on(
        "id" -> product.id,
        "ean" -> product.ean,
        "name" -> product.name,
        "description" -> product.description).
        executeUpdate()
      updateRows == 1
    }

  def delete(product: Product): Boolean =
    DB.withConnection { implicit connection =>
      val updateRows = SQL("delete from products where id = {id}").
        on("id" -> product.id).executeUpdate()
      updateRows == 0
    }

  // Squeryl
  import Database.{productsTable, stockItemsTable}
  def allQ: Query[Product] = from(productsTable) {
    product => select(product) orderBy(product.name.desc)
  }

  def findAll2: Iterable[Product] = inTransaction(
    allQ.toList
  )

  def productsInWarehouse(warehouse: Warehouse) = {
    join(productsTable, stockItemsTable)((product, stockItem) =>
      where(stockItem.warehouseId === warehouse.id).
        select(product).
        on(stockItem.productId === product.id)
    )
  }

  def productsInWarehouseByName(name: String,
                                warehouse: Warehouse): Query[Product] = {
    from(productsInWarehouse(warehouse)){product =>
      where(product.description like name).select(product)
    }
  }

  def insert2(product: Product): Product = inTransaction(
    productsTable.insert(product)
  )

  def update2(product: Product): Unit = {
    inTransaction(productsTable.update(product))
  }
}

case class Warehouse(id: Long, name: String) extends KeyedEntity[Long] {
  lazy val stockItems: OneToMany[StockItem] =
    Database.warehouseToStockItems.left(this)
}

case class StockItem(
  id: Long,
  productId: Long,
  warehouseId: Long,
  quantity: Long
) extends KeyedEntity[Long] {
  lazy val product: ManyToOne[Product] =
    Database.productToStockItems.right(this)

  lazy val warehouse: ManyToOne[Warehouse] =
    Database.warehouseToStockItems.right(this)
}

object StockItem {
  val stockItemParser: RowParser[StockItem] = {
    import anorm.SqlParser._
    import anorm.~
    long("id") ~ long("product_id") ~
      long("warehouse_id") ~ long("quantity") map {
      case id ~ productId ~ warehouseId ~ quantity =>
        StockItem(id, productId, warehouseId, quantity)
    }
  }
}

case class Preparation(
orderNumber: Int,
product: Product,
quantity: Int,
location: String)

object PickList {
  def find(warehouse: String): List[Preparation] = List()
}

case class OrderLine()

object Order {
  def backlog(warehouse: String): List[OrderLine] = List()
}

object Warehouse {
  def find(): List[String] = List()
}
