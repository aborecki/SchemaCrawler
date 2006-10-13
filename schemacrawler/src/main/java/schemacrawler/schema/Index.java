/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2006, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.schema;


import schemacrawler.crawl.SchemaCrawlerException;

/**
 * Represents an index.
 * 
 * @author sfatehi
 */
public interface Index
  extends DependantNamedObject
{

  /**
   * Cardinality. When the index type is statistic, then this is the
   * number of rows in the table; otherwise, it is the number of unique
   * values in the index.
   * 
   * @return Cardinality
   */
  int getCardinality();

  /**
   * Is the index unique.
   * 
   * @return Is the index unique
   */
  boolean isUnique();

  /**
   * Pages. When the index type is statistic, then this is the number of
   * pages used for the table, otherwise it is the number of pages used
   * for the current index.
   * 
   * @return Pages
   */
  int getPages();

  /**
   * Sort sequence.
   * 
   * @return Sort sequence
   */
  IndexSortSequence getSortSequence();

  /**
   * Index type.
   * 
   * @return Index type
   */
  IndexType getType();

  /**
   * List of columns in ordinal order.
   * 
   * @return Columns of the table.
   */
  Column[] getColumns();

  /**
   * Converts this index into a table constraint. If the index is not
   * unique, throws an exception.
   * 
   * @throws SchemaCrawlerException
   *         If the index is not null.
   * @return Table constraint, or null
   */
  TableConstraint asTableConstraint()
    throws SchemaCrawlerException;

}
