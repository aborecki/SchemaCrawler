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
package schemacrawler.server.jdbcodbc_nz;


import java.io.IOException;
import java.sql.Connection;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import schemacrawler.crawl.MetadataRetrievalStrategy;
import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.executable.commandline.PluginCommand;
import schemacrawler.tools.iosource.ClasspathInputResource;
import sf.util.SchemaCrawlerLogger;

public final class JDBCODBC_NZDatabaseConnector
  extends DatabaseConnector
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger.getLogger(
    JDBCODBC_NZDatabaseConnector.class.getName());

  public JDBCODBC_NZDatabaseConnector()
    throws IOException
  {
    super(new DatabaseServerType("jdbcodbc_nz", "NZ over JDBC/ODBC"),
          new ClasspathInputResource("/schemacrawler-jdbcodbc_nz.config.properties"),
          (informationSchemaViewsBuilder, connection) -> informationSchemaViewsBuilder
            .fromResourceFolder("/nz.information_schema"));
  }

  @Override
  public SchemaRetrievalOptionsBuilder getSchemaRetrievalOptionsBuilder(final Connection connection)
  {
    final SchemaRetrievalOptionsBuilder schemaRetrievalOptionsBuilder = super.getSchemaRetrievalOptionsBuilder(
      connection);
       schemaRetrievalOptionsBuilder.withTableRetrievalStrategy(
                 MetadataRetrievalStrategy.data_dictionary_all)
            //.withTableColumnRetrievalStrategy(
            //        MetadataRetrievalStrategy.data_dictionary_all)
            //.withPrimaryKeyRetrievalStrategy(
             //       MetadataRetrievalStrategy.data_dictionary_all)
            //.withForeignKeyRetrievalStrategy(
            //        MetadataRetrievalStrategy.data_dictionary_all)
            //.withIndexRetrievalStrategy(
            //        MetadataRetrievalStrategy.data_dictionary_all)
            //.withProcedureRetrievalStrategy(
            //        MetadataRetrievalStrategy.data_dictionary_all)
            //.withProcedureColumnRetrievalStrategy(
           //         MetadataRetrievalStrategy.data_dictionary_all)
            //.withFunctionRetrievalStrategy(
            //        MetadataRetrievalStrategy.data_dictionary_all)
            //.withFunctionColumnRetrievalStrategy(
             //       MetadataRetrievalStrategy.data_dictionary_all)
           ;
    return schemaRetrievalOptionsBuilder;
  }

  @Override
  public PluginCommand getHelpCommand()
  {
    final PluginCommand pluginCommand = super.getHelpCommand();
    pluginCommand.addOption("server",
                            "--server=jdbcodbc_nz%n"
                            + "Loads SchemaCrawler plug-in for Netezza over JDBC/ODBC",
                            String.class)
                 .addOption("host",
                            "Host name%n" + "Optional, defaults to localhost",
                            String.class)
                 .addOption("port",
                            "Port number%n" + "Optional, defaults to 50000",
                            Integer.class)
                 .addOption("database", "Database name", String.class);
    return pluginCommand;
  }

  @Override
  protected Predicate<String> supportsUrlPredicate()
  {
    return url -> Pattern.matches("jdbc:odbc:.*", url);
  }

}
