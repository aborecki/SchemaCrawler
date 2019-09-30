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
package schemacrawler.utility;


import static java.util.stream.Collectors.toMap;
import static sf.util.Utility.isBlank;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

public final class TypeMap
  implements Map<String, Class<?>>
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(TypeMap.class.getName());

  /**
   * The default mappings are from the JDBC Specification 4.2, Appendix
   * B - Data Type Conversion Tables, Table B-3 - Mapping from JDBC
   * Types to Java Object Types. A JDBC driver may override these
   * default mappings.
   */
  private static Map<SQLType, Class<?>> createDefaultTypeMap()
  {
    final Map<SQLType, Class<?>> defaultTypeMap = new HashMap<>();

    defaultTypeMap.put(JDBCType.ARRAY, java.sql.Array.class);
    defaultTypeMap.put(JDBCType.BIGINT, Long.class);
    defaultTypeMap.put(JDBCType.BINARY, byte[].class);
    defaultTypeMap.put(JDBCType.BIT, Boolean.class);
    defaultTypeMap.put(JDBCType.BLOB, java.sql.Blob.class);
    defaultTypeMap.put(JDBCType.BOOLEAN, Boolean.class);
    defaultTypeMap.put(JDBCType.CHAR, String.class);
    defaultTypeMap.put(JDBCType.CLOB, java.sql.Clob.class);
    defaultTypeMap.put(JDBCType.DATALINK, java.net.URL.class);
    defaultTypeMap.put(JDBCType.DATE, java.sql.Date.class);
    defaultTypeMap.put(JDBCType.DECIMAL, java.math.BigDecimal.class);
    defaultTypeMap.put(JDBCType.DISTINCT, Object.class);
    defaultTypeMap.put(JDBCType.DOUBLE, Double.class);
    defaultTypeMap.put(JDBCType.FLOAT, Double.class);
    defaultTypeMap.put(JDBCType.INTEGER, Integer.class);
    defaultTypeMap.put(JDBCType.JAVA_OBJECT, Object.class);
    defaultTypeMap.put(JDBCType.LONGNVARCHAR, String.class);
    defaultTypeMap.put(JDBCType.LONGVARBINARY, byte[].class);
    defaultTypeMap.put(JDBCType.LONGVARCHAR, String.class);
    defaultTypeMap.put(JDBCType.NCHAR, String.class);
    defaultTypeMap.put(JDBCType.NCLOB, java.sql.NClob.class);
    defaultTypeMap.put(JDBCType.NULL, Void.class);
    defaultTypeMap.put(JDBCType.NUMERIC, java.math.BigDecimal.class);
    defaultTypeMap.put(JDBCType.NVARCHAR, String.class);
    defaultTypeMap.put(JDBCType.OTHER, Object.class);
    defaultTypeMap.put(JDBCType.REAL, Float.class);
    defaultTypeMap.put(JDBCType.REF, java.sql.Ref.class);
    defaultTypeMap.put(JDBCType.REF_CURSOR, java.lang.Object.class);
    defaultTypeMap.put(JDBCType.ROWID, java.sql.RowId.class);
    defaultTypeMap.put(JDBCType.SMALLINT, Integer.class);
    defaultTypeMap.put(JDBCType.SQLXML, java.sql.SQLXML.class);
    defaultTypeMap.put(JDBCType.STRUCT, java.sql.Struct.class);
    defaultTypeMap.put(JDBCType.TIME, java.sql.Time.class);
    defaultTypeMap.put(JDBCType.TIMESTAMP, java.sql.Timestamp.class);
    defaultTypeMap.put(JDBCType.TIMESTAMP_WITH_TIMEZONE,
                       java.time.OffsetDateTime.class);
    defaultTypeMap.put(JDBCType.TIME_WITH_TIMEZONE, java.time.OffsetTime.class);
    defaultTypeMap.put(JDBCType.TINYINT, Integer.class);
    defaultTypeMap.put(JDBCType.VARBINARY, byte[].class);
    defaultTypeMap.put(JDBCType.VARCHAR, String.class);

    return defaultTypeMap;
  }

  private final Map<String, Class<?>> sqlTypeMap;

  public TypeMap()
  {
    sqlTypeMap = new HashMap<>();

    final Map<SQLType, Class<?>> defaultTypeMap = createDefaultTypeMap();
    for (final Entry<SQLType, Class<?>> sqlTypeMapping: defaultTypeMap
      .entrySet())
    {
      sqlTypeMap.put(sqlTypeMapping.getKey().getName(),
                     sqlTypeMapping.getValue());
    }
  }

  public TypeMap(final Connection connection)
  {
    this();

    if (connection != null)
    {
      // Override and add mappings from the connection
      try
      {
        Map<String, Class<?>> typeMap = null;
		try
		{
			typeMap=connection.getTypeMap();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING,
                   "Could not obtain data type map from connection",
                   e);
		}
        if (typeMap != null && !typeMap.isEmpty())
        {
          sqlTypeMap.putAll(typeMap);
        }
      }
      catch (final Exception e)
      {
        // Catch all exceptions, since even though most JDBC drivers
        // would throw SQLException, but the Sybase Adaptive
        // Server driver throws UnimplementedOperationException
        LOGGER.log(Level.WARNING,
                   "Could not obtain data type map from connection",
                   e);
      }
    }
  }

  public TypeMap(final Map<String, Class<?>> sqlTypeMap)
  {
    if (sqlTypeMap == null)
    {
      this.sqlTypeMap = new HashMap<>();
    }
    else
    {
      this.sqlTypeMap = new HashMap<>(sqlTypeMap);
    }
  }

  @Override
  public void clear()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsKey(final Object key)
  {
    return sqlTypeMap.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value)
  {
    return sqlTypeMap.containsValue(value);
  }

  @Override
  public Set<Entry<String, Class<?>>> entrySet()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(final Object o)
  {
    return sqlTypeMap.equals(o);
  }

  @Override
  public Class<?> get(final Object key)
  {
    if (containsKey(key))
    {
      return sqlTypeMap.get(key);
    }
    else
    {
      return Object.class;
    }
  }

  /**
   * Gets the Java type mapping for a data type. If no mapping exists,
   * returns null. If a class name is passed in, it overrides the
   * mapping in the type map.
   *
   * @param typeName
   *        Type name to find a mapping for.
   * @param className
   *        Overridden class name
   * @return Mapped class
   */
  public Class<?> get(final String typeName, final String className)
  {
    if (isBlank(className))
    {
      return sqlTypeMap.get(typeName);
    }
    else
    {
      try
      {
        return Class.forName(className);
      }
      catch (final ClassNotFoundException e)
      {
        LOGGER
          .log(Level.WARNING,
               new StringFormat("Could not obtain class mapping for data type <%s>",
                                typeName),
               e);
        return null;
      }
    }
  }

  @Override
  public int hashCode()
  {
    return sqlTypeMap.hashCode();
  }

  @Override
  public boolean isEmpty()
  {
    return sqlTypeMap.isEmpty();
  }

  @Override
  public Set<String> keySet()
  {
    return new HashSet<>(sqlTypeMap.keySet());
  }

  @Override
  public Class<?> put(final String key, final Class<?> value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(final Map<? extends String, ? extends Class<?>> m)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> remove(final Object key)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size()
  {
    return sqlTypeMap.size();
  }

  @Override
  public String toString()
  {
    final Map<String, String> typeClassNameMap = sqlTypeMap.entrySet().stream()
      .collect(toMap(Map.Entry::getKey, e -> e.getValue().getCanonicalName()));
    return typeClassNameMap.toString();
  }

  @Override
  public Collection<Class<?>> values()
  {
    return new HashSet<>(sqlTypeMap.values());
  }

}
