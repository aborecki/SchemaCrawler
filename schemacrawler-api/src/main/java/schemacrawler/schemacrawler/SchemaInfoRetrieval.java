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

package schemacrawler.schemacrawler;


import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.DatabaseObjectInfoRetrieval.base;
import static schemacrawler.schemacrawler.DatabaseObjectInfoRetrieval.database;
import static schemacrawler.schemacrawler.DatabaseObjectInfoRetrieval.other;
import static schemacrawler.schemacrawler.DatabaseObjectInfoRetrieval.routine;
import static schemacrawler.schemacrawler.DatabaseObjectInfoRetrieval.table;
import static schemacrawler.schemacrawler.InfoLevel.detailed;
import static schemacrawler.schemacrawler.InfoLevel.maximum;
import static schemacrawler.schemacrawler.InfoLevel.minimum;
import static schemacrawler.schemacrawler.InfoLevel.standard;
import static sf.util.Utility.toSnakeCase;

public enum SchemaInfoRetrieval
{
 retrieveAdditionalColumnAttributes(table, maximum),
 retrieveAdditionalDatabaseInfo(database, maximum),
 retrieveAdditionalJdbcDriverInfo(database, maximum),
 retrieveAdditionalTableAttributes(table, maximum),
 retrieveColumnDataTypes(base, standard),
 retrieveDatabaseInfo(database, minimum),
 retrieveForeignKeyDefinitions(table, maximum),
 retrieveForeignKeys(table, standard),
 retrieveIndexColumnInformation(table, maximum),
 retrieveIndexes(table, standard),
 retrieveIndexInformation(table, maximum),
 retrievePrimaryKeyDefinitions(table, maximum),
 retrieveRoutineParameters(routine, standard),
 retrieveRoutineInformation(routine, detailed),
 retrieveRoutines(routine, minimum),
 retrieveSequenceInformation(other, maximum),
 retrieveServerInfo(database, maximum),
 retrieveSynonymInformation(other, maximum),
 retrieveTableColumnPrivileges(table, maximum),
 retrieveTableColumns(table, standard),
 retrieveTableConstraintDefinitions(table, detailed),
 retrieveTableConstraintInformation(table, detailed),
 retrieveTableDefinitionsInformation(table, maximum),
 retrieveTablePrivileges(table, maximum),
 retrieveTables(table, minimum),
 retrieveTriggerInformation(table, detailed),
 retrieveUserDefinedColumnDataTypes(other, detailed),
 retrieveViewInformation(table, detailed);

  private static final String SC_SCHEMA_INFO_LEVEL = "schemacrawler.schema_info_level.";

  private final InfoLevel infoLevel;
  private final DatabaseObjectInfoRetrieval databaseObjectInfoRetrieval;

  private SchemaInfoRetrieval(final DatabaseObjectInfoRetrieval databaseObjectInfoRetrieval,
                              final InfoLevel infoLevel)
  {
    this.infoLevel = requireNonNull(infoLevel);
    this.databaseObjectInfoRetrieval = requireNonNull(databaseObjectInfoRetrieval);
  }

  public DatabaseObjectInfoRetrieval getDatabaseObjectInfoRetrieval()
  {
    return databaseObjectInfoRetrieval;
  }

  public InfoLevel getInfoLevel()
  {
    return infoLevel;
  }

  public String getKey()
  {
    return SC_SCHEMA_INFO_LEVEL + toSnakeCase(name());
  }

}
