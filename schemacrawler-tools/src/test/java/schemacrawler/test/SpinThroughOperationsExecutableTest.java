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
package schemacrawler.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.hasSameContentAndTypeAs;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.TestAssertNoSystemErrOutput;
import schemacrawler.test.utility.TestAssertNoSystemOutOutput;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.test.utility.TestUtility;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.text.operation.Operation;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;

@ExtendWith(TestAssertNoSystemErrOutput.class)
@ExtendWith(TestAssertNoSystemOutOutput.class)
@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class SpinThroughOperationsExecutableTest
{

  private static final String SPIN_THROUGH_OPERATIONS_OUTPUT = "spin_through_operations_output/";

  @BeforeAll
  public static void clean()
    throws Exception
  {
    TestUtility.clean(SPIN_THROUGH_OPERATIONS_OUTPUT);
  }

  private static Stream<InfoLevel> infoLevels()
  {
    return Arrays.stream(InfoLevel.values())
                 .filter(infoLevel -> infoLevel != InfoLevel.unknown);
  }

  private static Stream<Operation> operations()
  {
    return Arrays.stream(Operation.values());
  }

  private static Stream<TextOutputFormat> outputFormats()
  {
    return Arrays.stream(new TextOutputFormat[] {
      TextOutputFormat.text, TextOutputFormat.html, TextOutputFormat.json
    });
  }

  private static String referenceFile(final Operation operation,
                                      final InfoLevel infoLevel,
                                      final OutputFormat outputFormat)
  {
    final String referenceFile = String.format("%d%d.%s_%s.%s",
                                               operation.ordinal(),
                                               infoLevel.ordinal(),
                                               operation,
                                               infoLevel,
                                               outputFormat.getFormat());
    return referenceFile;
  }

  @Test
  public void spinThroughOperationsExecutable(final Connection connection)
    throws Exception
  {

    assertAll(infoLevels().flatMap(infoLevel -> outputFormats().flatMap(
      outputFormat -> operations().map(operation -> () -> {

        // Special case where no output is generated
        if (infoLevel == InfoLevel.minimum && operation == Operation.dump)
        {
          return;
        }

        final String referenceFile = referenceFile(operation,
                                                   infoLevel,
                                                   outputFormat);

        final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
          .builder()
          .withSchemaInfoLevel(infoLevel.toSchemaInfoLevel())
          .includeAllSequences()
          .includeAllSynonyms()
          .includeAllRoutines();
        final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder
          .toOptions();

        final SchemaTextOptionsBuilder schemaTextOptionsBuilder = SchemaTextOptionsBuilder
          .builder();
        schemaTextOptionsBuilder.noInfo(false);

        final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(
          operation.name());
        executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
        executable.setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());

        assertThat(outputOf(executableExecution(connection,
                                                executable,
                                                outputFormat)),
                   hasSameContentAndTypeAs(classpathResource(
                     SPIN_THROUGH_OPERATIONS_OUTPUT + referenceFile),
                                           outputFormat));

      }))));
  }

}
