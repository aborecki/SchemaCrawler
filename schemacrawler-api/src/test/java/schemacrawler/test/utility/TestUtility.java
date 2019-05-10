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
package schemacrawler.test.utility;


import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static sf.util.IOUtility.isFileReadable;
import static sf.util.Utility.isBlank;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.apache.commons.io.FileUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import sf.util.IOUtility;

public final class TestUtility
{

  public static void clean(final String dirname)
    throws Exception
  {
    FileUtils
      .deleteDirectory(buildDirectory().resolve("unit_tests_results_output")
                         .resolve(dirname).toFile());
  }

  public static List<String> compareCompressedOutput(final String referenceFile,
                                                     final Path testOutputTempFile,
                                                     final String outputFormat)
    throws Exception
  {
    return compareOutput(referenceFile, testOutputTempFile, outputFormat, true);
  }

  public static List<String> compareOutput(final String referenceFile,
                                           final Path testOutputTempFile,
                                           final String outputFormat)
    throws Exception
  {
    return compareOutput(referenceFile,
                         testOutputTempFile,
                         outputFormat,
                         false);
  }

  public static List<String> compareOutput(final String referenceFile,
                                           final Path testOutputTempFile,
                                           final String outputFormat,
                                           final boolean isCompressed)
    throws Exception
  {

    requireNonNull(referenceFile, "Reference file is not defined");
    requireNonNull(testOutputTempFile, "Output file is not defined");
    requireNonNull(outputFormat, "Output format is not defined");

    if (!isFileReadable(testOutputTempFile))
    {
      return Collections
        .singletonList("Output file not created - " + testOutputTempFile);
    }

    final List<String> failures = new ArrayList<>();

    final boolean contentEquals;
    final Reader referenceReader = readerForResource(referenceFile,
                                                     UTF_8,
                                                     isCompressed);
    if (referenceReader == null)

    {
      contentEquals = false;
    }
    else if ("png".equals(outputFormat))
    {
      contentEquals = true;
    }
    else
    {
      final Reader fileReader = readerForFile(testOutputTempFile, isCompressed);
      final Predicate<String> linesFilter = new SvgElementFilter()
        .and(new NeuteredLinesFilter());
      contentEquals = contentEquals(referenceReader,
                                    fileReader,
                                    failures,
                                    linesFilter);
    }

    if ("html".equals(outputFormat))
    {
      validateXML(testOutputTempFile, failures);
    }
    if ("htmlx".equals(outputFormat))
    {
      validateXML(testOutputTempFile, failures);
    }
    else if ("json".equals(outputFormat))
    {
      validateJSON(testOutputTempFile, failures);
    }
    else if ("png".equals(outputFormat))
    {
      validateDiagram(testOutputTempFile);
    }

    if (!contentEquals)
    {
      final Path testOutputTargetFilePath = buildDirectory()
        .resolve("unit_tests_results_output").resolve(referenceFile);
      createDirectories(testOutputTargetFilePath.getParent());
      deleteIfExists(testOutputTargetFilePath);
      move(testOutputTempFile, testOutputTargetFilePath, REPLACE_EXISTING);

      if (!contentEquals)
      {
        failures.add("Output does not match");
      }

      failures.add("Actual output in " + testOutputTargetFilePath);
      System.err.println(testOutputTargetFilePath);
    }
    else
    {
      try
      {
        delete(testOutputTempFile);
      }
      catch (final IOException e)
      {
        System.err.println("Could not delete file, " + testOutputTempFile);
        e.printStackTrace();
      }
    }

    return failures;
  }

  public static Path copyResourceToTempFile(final String resource)
    throws IOException
  {
    if (isBlank(resource))
    {
      throw new IOException("Cannot read empty resource");
    }

    try (final InputStream resourceStream = TestUtility.class
      .getResourceAsStream(resource))
    {
      requireNonNull(resourceStream, "Resource not found, " + resource);
      return writeToTempFile(resourceStream);
    }
  }

  public static String[] flattenCommandlineArgs(final Map<String, String> argsMap)
  {
    final List<String> argsList = new ArrayList<>();
    for (final Map.Entry<String, String> arg : argsMap.entrySet())
    {
      final String key = arg.getKey();
      final String value = arg.getValue();
      if (value != null)
      {
        argsList.add(String.format("-%s=%s", key, value));
      }
      else
      {
        argsList.add(String.format("-%s", key));
      }
    }
    final String[] args = argsList.toArray(new String[0]);
    return args;
  }

  public static Reader readerForResource(final String resource,
                                         final Charset encoding)
    throws IOException
  {
    return readerForResource(resource, encoding, false);
  }

  public static void validateDiagram(final Path diagramFile)
    throws IOException
  {
    assertThat("Diagram file not created", exists(diagramFile), is(true));
    assertThat("Diagram file has 0 bytes size",
               size(diagramFile),
               greaterThan(0L));
  }

  private static Path buildDirectory()
    throws Exception
  {
    final StackTraceElement ste = currentMethodStackTraceElement();
    final Class<?> callingClass = Class.forName(ste.getClassName());
    final Path codePath = Paths
      .get(callingClass.getProtectionDomain().getCodeSource().getLocation()
             .toURI()).normalize().toAbsolutePath();
    final boolean isInTarget = codePath.toString().contains("target");
    if (!isInTarget)
    {
      throw new RuntimeException("Not in build directory, " + codePath);
    }
    final Path directory = codePath.resolve("..");
    return directory.normalize().toAbsolutePath();
  }

  private static boolean contentEquals(final Reader expectedInputReader,
                                       final Reader actualInputReader,
                                       final List<String> failures,
                                       final Predicate<String> keepLines)
    throws Exception
  {
    if (expectedInputReader == null || actualInputReader == null)
    {
      return false;
    }

    try (final Stream<String> expectedLinesStream = new BufferedReader(
      expectedInputReader).lines();
      final Stream<String> actualLinesStream = new BufferedReader(
        actualInputReader).lines())
    {
      final Iterator<String> expectedLinesIterator = expectedLinesStream
        .filter(keepLines).iterator();
      final Iterator<String> actualLinesIterator = actualLinesStream
        .filter(keepLines).iterator();

      while (expectedLinesIterator.hasNext() && actualLinesIterator.hasNext())
      {
        final String expectedline = expectedLinesIterator.next();
        final String actualLine = actualLinesIterator.next();

        if (!expectedline.equals(actualLine))
        {
          final StringBuilder buffer = new StringBuilder();
          buffer.append(">> expected followed by actual:").append("\n");
          buffer.append(expectedline).append("\n");
          buffer.append(actualLine).append("\n");

          final String lineMiscompare = buffer.toString();
          failures.add(lineMiscompare);
          System.out.println(lineMiscompare);

          return false;
        }
      }

      if (actualLinesIterator.hasNext())
      {
        return false;
      }
      if (expectedLinesIterator.hasNext())
      {
        return false;
      }

      return true;
    }
  }

  private static StackTraceElement currentMethodStackTraceElement()
  {
    final Pattern baseTestClassName = Pattern.compile(".*\\.Base.*Test");
    final Pattern testClassName = Pattern.compile(".*\\.[A-Z].*Test");

    final StackTraceElement[] stackTrace = Thread.currentThread()
      .getStackTrace();
    for (final StackTraceElement stackTraceElement : stackTrace)
    {
      final String className = stackTraceElement.getClassName();
      if (testClassName.matcher(className).matches() && !baseTestClassName
        .matcher(className).matches())
      {
        return stackTraceElement;
      }
    }

    return null;
  }

  private static void fastChannelCopy(final ReadableByteChannel src,
                                      final WritableByteChannel dest)
    throws IOException
  {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
    while (src.read(buffer) != -1)
    {
      // prepare the buffer to be drained
      buffer.flip();
      // write to the channel, may block
      dest.write(buffer);
      // If partial transfer, shift remainder down
      // If buffer is empty, same as doing clear()
      buffer.compact();
    }
    // EOF will leave buffer in fill state
    buffer.flip();
    // make sure the buffer is fully drained.
    while (buffer.hasRemaining())
    {
      dest.write(buffer);
    }
  }

  private static Reader openNewCompressedInputReader(final InputStream inputStream,
                                                     final Charset charset)
    throws IOException
  {
    final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    zipInputStream.getNextEntry();
    return new InputStreamReader(zipInputStream, charset);
  }

  private static Reader readerForFile(final Path testOutputTempFile)
    throws IOException
  {
    return readerForFile(testOutputTempFile, false);
  }

  private static Reader readerForFile(final Path testOutputTempFile,
                                      final boolean isCompressed)
    throws IOException
  {

    final BufferedReader bufferedReader;
    if (isCompressed)
    {
      final ZipInputStream inputStream = new ZipInputStream(newInputStream(
        testOutputTempFile,
        StandardOpenOption.READ));
      inputStream.getNextEntry();

      bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                                                                UTF_8));
    }
    else
    {
      bufferedReader = newBufferedReader(testOutputTempFile, UTF_8);
    }
    return bufferedReader;
  }

  private static Reader readerForResource(final String resource,
                                          final Charset encoding,
                                          final boolean isCompressed)
    throws IOException
  {
    final InputStream inputStream = TestUtility.class
      .getResourceAsStream("/" + resource);
    final Reader reader;
    if (inputStream != null)
    {
      final Charset charset;
      if (encoding == null)
      {
        charset = UTF_8;
      }
      else
      {
        charset = encoding;
      }
      if (isCompressed)
      {
        reader = openNewCompressedInputReader(inputStream, charset);
      }
      else
      {
        reader = new InputStreamReader(inputStream, charset);
      }
    }
    else
    {
      reader = null;
    }
    return reader;
  }

  private static boolean validateJSON(final Path testOutputFile,
                                      final List<String> failures)
    throws FileNotFoundException, SAXException, IOException
  {
    final JsonElement jsonElement;
    try (final Reader reader = readerForFile(testOutputFile);
      final JsonReader jsonReader = new JsonReader(reader))
    {
      jsonElement = new JsonParser().parse(jsonReader);
      if (jsonReader.peek() != JsonToken.END_DOCUMENT)
      {
        failures.add("JSON document was not fully consumed.");
      }
    }
    catch (final Exception e)
    {
      failures.add(e.getMessage());
      return false;
    }

    final int size;
    if (jsonElement.isJsonObject())
    {
      size = jsonElement.getAsJsonObject().entrySet().size();
    }
    else if (jsonElement.isJsonArray())
    {
      size = jsonElement.getAsJsonArray().size();
    }
    else
    {
      size = 0;
    }

    if (size == 0)
    {
      failures.add("Invalid JSON string");
    }

    return failures.isEmpty();
  }

  private static void validateXML(final Path testOutputFile,
                                  final List<String> failures)
    throws Exception
  {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(true);

    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new ErrorHandler()
    {
      @Override
      public void error(final SAXParseException e)
        throws SAXException
      {
        failures.add(e.getMessage());
      }

      @Override
      public void fatalError(final SAXParseException e)
        throws SAXException
      {
        failures.add(e.getMessage());
      }

      @Override
      public void warning(final SAXParseException e)
        throws SAXException
      {
        failures.add(e.getMessage());
      }
    });
    builder.parse(new InputSource(readerForFile(testOutputFile)));
  }

  private static Path writeToTempFile(final InputStream resourceStream)
    throws IOException, FileNotFoundException
  {
    final Path tempFile = IOUtility.createTempFilePath("resource", "data")
      .normalize().toAbsolutePath();

    try (final OutputStream tempFileStream = newOutputStream(tempFile,
                                                             WRITE,
                                                             TRUNCATE_EXISTING,
                                                             CREATE))
    {
      fastChannelCopy(Channels.newChannel(resourceStream),
                      Channels.newChannel(tempFileStream));
    }

    return tempFile;
  }

  private TestUtility()
  {
    // Prevent instantiation
  }

}
