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
package schemacrawler.tools.iosource;


import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.logging.Level;

import sf.util.SchemaCrawlerLogger;

public class ReaderInputResource
  implements InputResource
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(ReaderInputResource.class.getName());

  private final Reader reader;

  public ReaderInputResource(final Reader reader)
  {
    this.reader = requireNonNull(reader, "No reader provided");
  }

  @Override
  public Reader openNewInputReader(final Charset charset)
    throws IOException
  {
    LOGGER.log(Level.INFO, "Input to provided reader");
    return new InputReader(getDescription(), new BufferedReader(reader), false);
  }

  @Override
  public String toString()
  {
    return "<reader>";
  }

}
