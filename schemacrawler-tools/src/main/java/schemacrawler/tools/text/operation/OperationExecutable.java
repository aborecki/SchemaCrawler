/*
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2014, Sualeh Fatehi.
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

package schemacrawler.tools.text.operation;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schema.Database;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.executable.BaseStagedExecutable;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.traversal.DataTraversalHandler;

/**
 * Basic SchemaCrawler executor.
 * 
 * @author Sualeh Fatehi
 */
public final class OperationExecutable
  extends BaseStagedExecutable
{

  private static final Logger LOGGER = Logger
    .getLogger(OperationExecutable.class.getName());

  private OperationOptions operationOptions;

  public OperationExecutable(final String command)
  {
    super(command);
  }

  public final OperationOptions getOperationOptions()
  {
    final OperationOptions operationOptions;
    if (this.operationOptions == null)
    {
      operationOptions = new OperationOptions(additionalConfiguration);
    }
    else
    {
      operationOptions = this.operationOptions;
    }
    return operationOptions;
  }

  public final void setOperationOptions(final OperationOptions operationOptions)
  {
    this.operationOptions = operationOptions;
  }

  @Override
  protected void executeOn(final Database database, final Connection connection)
    throws Exception
  {
    final DataTraversalHandler handler = getDataTraversalHandler();
    final Query query = getQuery();

    try (final Statement statement = createStatement(connection);)
    {

      handler.begin();

      handler.handleInfoStart();
      handler.handle(database.getSchemaCrawlerInfo());
      handler.handle(database.getDatabaseInfo());
      handler.handle(database.getJdbcDriverInfo());
      handler.handleInfoEnd();

      if (query.isQueryOver())
      {
        final Collection<Table> tables = database.getTables();

        for (final Table table: tables)
        {
          final String sql = query
            .getQueryForTable(table, getOperationOptions()
              .isAlphabeticalSortForTableColumns());

          LOGGER.log(Level.FINE,
                     String.format("Executing query for table %s: %s",
                                   table.getFullName(),
                                   sql));
          try (final ResultSet results = executeSql(statement, sql);)
          {
            handler.handleData(table, results);
          }
        }
      }
      else
      {
        final String sql = query.getQuery();
        try (final ResultSet results = executeSql(statement, sql);)
        {
          handler.handleData(query, results);
        }
      }

      handler.end();

    }
    catch (final SQLException e)
    {
      throw new SchemaCrawlerException("Cannot perform operation", e);
    }
  }

  private DataTraversalHandler getDataTraversalHandler()
    throws SchemaCrawlerException
  {
    final Operation operation = getOperation();

    final OperationOptions operationOptions = getOperationOptions();
    final DataTraversalHandler formatter;
    final OutputFormat outputFormat = outputOptions.getOutputFormat();
    if (outputFormat == OutputFormat.json)
    {
      formatter = new DataJsonFormatter(operation,
                                        operationOptions,
                                        outputOptions);
    }
    else
    {
      formatter = new DataTextFormatter(operation,
                                        operationOptions,
                                        outputOptions);
    }
    return formatter;
  }

  /**
   * Determine the operation, or whether this command is a query.
   */
  private Operation getOperation()
  {
    Operation operation = null;
    try
    {
      operation = Operation.valueOf(command);
    }
    catch (final IllegalArgumentException e)
    {
      operation = null;
    }
    return operation;
  }

  private Query getQuery()
  {
    final Operation operation = getOperation();
    final Query query;
    if (operation == null)
    {
      final String queryName = command;
      final String queryString;
      if (additionalConfiguration != null)
      {
        queryString = additionalConfiguration.get(queryName);
      }
      else
      {
        queryString = null;
      }
      query = new Query(queryName, queryString);
    }
    else
    {
      query = operation.getQuery();
    }

    return query;
  }

  private Statement createStatement(Connection connection)
    throws SchemaCrawlerException, SQLException
  {
    if (connection == null)
    {
      throw new SchemaCrawlerException("No connection provided");
    }
    if (connection.isClosed())
    {
      throw new SchemaCrawlerException("Connection is closed");
    }

    return connection.createStatement();
  }

  private ResultSet executeSql(final Statement statement, final String sql)
    throws SchemaCrawlerException
  {
    ResultSet results = null;
    if (statement == null)
    {
      return results;
    }

    try
    {
      final boolean hasResults = statement.execute(sql);
      if (hasResults)
      {
        results = statement.getResultSet();
        return results;
      }
      else
      {
        LOGGER.log(Level.WARNING, "No results for: " + sql);
        return null;
      }
    }
    catch (final SQLException e)
    {
      LOGGER.log(Level.WARNING, "Error executing: " + sql, e);
      return null;
    }
  }

}
