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


import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import picocli.CommandLine.*;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.tools.commandline.shell.AvailableCommandsCommand;
import schemacrawler.tools.commandline.state.SchemaCrawlerShellState;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.integration.graph.GraphOutputFormat;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;
import sf.util.ObjectToStringFormat;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

@Command(name = "execute",
         header = "** Execute a SchemaCrawler command",
         description = {
           ""
         },
         headerHeading = "",
         synopsisHeading = "Shell Command:%n",
         customSynopsis = {
           "execute"
         },
         optionListHeading = "Options:%n")
public class ExecuteCommand
  implements Runnable
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger.getLogger(
    AvailableCommandsCommand.class.getName());

  private final SchemaCrawlerShellState state;

  @Mixin
  private CommandOptions commandOptions;
  @Mixin
  private CommandOutputOptions commandOutputOptions;
  @Spec
  private Model.CommandSpec spec;

  public ExecuteCommand(final SchemaCrawlerShellState state)
  {
    this.state = requireNonNull(state, "No state provided");
  }

  @Override
  public void run()
  {
    if (!state.isLoaded())
    {
      throw new ExecutionException(spec.commandLine(),
                                   "No database metadata is loaded");
    }

    Connection connection = null;
    if (state.isConnected())
    {
      connection = state.getDataSource().get();
    }

    try
    {
      final OutputOptionsBuilder outputOptionsBuilder = OutputOptionsBuilder.builder()
                                                                            .fromConfig(
                                                                              state
                                                                                .getAdditionalConfiguration());
      if (commandOutputOptions.getOutputFile().isPresent())
      {
        outputOptionsBuilder.withOutputFile(commandOutputOptions.getOutputFile()
                                                                .get());
      }
      else
      {
        outputOptionsBuilder.withConsoleOutput();
      }
      commandOutputOptions.getOutputFormatValue()
                          .ifPresent(outputOptionsBuilder::withOutputFormatValue);
      commandOutputOptions.getTitle().ifPresent(outputOptionsBuilder::title);

      final SchemaCrawlerOptions schemaCrawlerOptions = state.getSchemaCrawlerOptionsBuilder()
                                                             .toOptions();
      final SchemaRetrievalOptions schemaRetrievalOptions = state.getSchemaRetrievalOptionsBuilder()
                                                                 .toOptions();
      final OutputOptions outputOptions = outputOptionsBuilder.toOptions();
      final Config additionalConfiguration = state.getAdditionalConfiguration();

      // Output file name has to be specified for diagrams
      // (Check after output options have been built)
      if (
        GraphOutputFormat.isSupportedFormat(outputOptions.getOutputFormatValue())
        && !commandOutputOptions.getOutputFile().isPresent())
      {
        throw new RuntimeException(
          "Output file has to be specified for schema diagrams");
      }

      final Catalog catalog = state.getCatalog();
      final String command = commandOptions.getCommand();

      LOGGER.log(Level.INFO,
                 new StringFormat("Executing SchemaCrawler command <%s>",
                                  command));
      LOGGER.log(Level.INFO,
                 new ObjectToStringFormat(outputOptions));

      final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(
        command);
      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
      executable.setOutputOptions(outputOptions);
      executable.setAdditionalConfiguration(additionalConfiguration);
      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);

      executable.setConnection(connection);
      executable.setCatalog(catalog);

      executable.execute();
    }
    catch (final Exception e)
    {
      throw new ExecutionException(spec.commandLine(),
                                   "Cannot execute SchemaCrawler command",
                                   e);
    }
    finally
    {
      if (connection != null)
      {
        try
        {
          connection.close();
        }
        catch (final SQLException e)
        {
          LOGGER.log(Level.WARNING,
                     "Could not close connection after executing SchemaCrawler command",
                     e);
        }
      }
    }
  }

}
