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
import static schemacrawler.test.utility.FileHasContent.*;
import static schemacrawler.test.utility.ScriptTestUtility.scriptExecution;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import schemacrawler.test.utility.TestAssertNoSystemErrOutput;
import schemacrawler.test.utility.TestAssertNoSystemOutOutput;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;

@ExtendWith(TestAssertNoSystemErrOutput.class)
@ExtendWith(TestAssertNoSystemOutOutput.class)
@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class ScriptingTest
{

  @Test
  public void executableGroovy(final Connection connection)
    throws Exception
  {
    assertThat(outputOf(scriptExecution(connection, "/plaintextschema.groovy")),
               hasSameContentAs(classpathResource("script_output.txt")));
  }

  @Test
  public void executableJavaScript(final Connection connection)
    throws Exception
  {
    assertThat(outputOf(scriptExecution(connection, "/plaintextschema.js")),
               hasSameContentAs(classpathResource("script_output.txt")));
  }

  @Test
  public void executablePython(final Connection connection)
    throws Exception
  {
    System.setProperty("python.console.encoding", "UTF-8");
    assertThat(outputOf(scriptExecution(connection, "/plaintextschema.py")),
               hasSameContentAs(classpathResource("script_output.txt")));
  }

  @Test
  public void executableRuby(final Connection connection)
    throws Exception
  {
    assertThat(outputOf(scriptExecution(connection, "/plaintextschema.rb")),
               hasSameContentAs(classpathResource("script_output_rb.txt")));
  }

}
