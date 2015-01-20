/**
 * Created by jean on 20.11.2014.
 */
package net.yested.bootstrap

import net.yested.HTMLComponent
import java.util.ArrayList
import net.yested.Span
import net.yested.with
import net.yested.Component
import net.yested.createElement
import net.yested.appendComponent
import net.yested.THead
import net.yested.TBody
import net.yested.removeChildByName

public data class Column<T>(
        val label: HTMLComponent.() -> Unit,
        val render: HTMLComponent.(T) -> Unit,
        val sortFunction:(T, T) -> Int,
        val align:Align = Align.LEFT,
        val defaultSort:Boolean = false,
        val defaultSortOrderAsc:Boolean = true)

public class ColumnHeader<T>(val column:Column<T>, sortFunction:(Column<T>) -> Unit) : HTMLComponent("span") {

    var sortOrderAsc:Boolean = column.defaultSortOrderAsc
    var arrowPlaceholder = Span();

    {
        element.setAttribute("style", "cursor: pointer;")

        column.label()
        +arrowPlaceholder

        onclick = {
            sortFunction(column)
        }
    }

    fun updateSorting(sorteByColumn:Column<T>?, sortAscending:Boolean) {
        if (sorteByColumn != column) {
            arrowPlaceholder.setContent("")
        } else {
            arrowPlaceholder.setChild(Glyphicon("arrow-${if (sortAscending) "up" else "down"}"))
        }
    }

}

public class Grid<T>(val columns:Array<Column<T>>) : Component {

    override val element = createElement("table")

    private var sortColumn:Column<T>? = null
    private var asc:Boolean = true;
    private val arrowsPlaceholders = ArrayList<Span>();
    private var columnHeaders:List<ColumnHeader<T>>? = null

    {
        element.className = "table table-striped table-hover table-condensed"
        columnHeaders = columns.map { ColumnHeader(column = it, sortFunction = { sortByColumn(it)}) }
        renderHeader()
        sortColumn = (columns.filter { it.defaultSort } : Iterable<Column<T>>).firstOrNull()
        asc = sortColumn?.defaultSortOrderAsc ?: true
        setSortingArrow()
    }

    private var dataList: List<T>? = null

    public var list: List<T>?
        get() = dataList
        set(value) {
            dataList = value
            displayData()
        }

    private fun setSortingArrow() {
        columnHeaders!!.forEach { it.updateSorting(sortColumn, asc) }
    }

    private fun sortByColumn(column:Column<T>):Unit {
        if (column == sortColumn) {
            asc = !asc;
        } else {
            asc = true;
            sortColumn = column
        }
        displayData()
        setSortingArrow()
    }

    private fun renderHeader() {
        element.appendComponent(THead() with
             {
                tr {
                    columnHeaders!!.forEach { columnHeader ->
                        th {
                            "class" .. "text-${columnHeader.column.align.code}";
                            +columnHeader
                        }
                    }
                }
            })
    }

    private fun sortData(toSort:List<T>):List<T> {
        return toSort.sortBy(object: java.util.Comparator<T> {
            override fun compare(obj1: T, obj2: T): Int {
                return (sortColumn!!.sortFunction(obj1, obj2)) * (if (asc) 1 else -1)
            }
        })
    }

    private fun displayData() {
        element.removeChildByName("tbody")
        dataList?.let {

            val values = if (sortColumn != null) sortData(dataList!!) else dataList!!

            element.appendComponent(TBody() with
                 {
                    values.forEach { item ->
                        tr {
                            columns.forEach { column ->
                                td {
                                    "class" .. "text-${column.align.code}";
                                    column.render(item) } }
                        }
                    }
                })

        }
    }

}