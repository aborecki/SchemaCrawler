/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.tools.commandline.command;


import picocli.CommandLine.Option;
import schemacrawler.schemacrawler.Config;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseConnectorRegistry;

public class DatabaseUrlConnectionOptions
  implements DatabaseConnectable
{

  @Option(names = {
    "--url"
  },
          required = true,
          description = "JDBC connection URL to the database")
  private String connectionUrl;

  @Override
  public DatabaseConnector getDatabaseConnector()
  {
    final DatabaseConnectorRegistry databaseConnectorRegistry = DatabaseConnectorRegistry
      .getDatabaseConnectorRegistry();
    return databaseConnectorRegistry.lookupDatabaseConnectorFromUrl(
      connectionUrl);
  }

  @Override
  public DatabaseConnectionSource toDatabaseConnectionSource(final Config config)
  {
    final DatabaseConnectionSource databaseConnectionSource = new DatabaseConnectionSource(
      connectionUrl,
      config);
    return databaseConnectionSource;
  }

}
