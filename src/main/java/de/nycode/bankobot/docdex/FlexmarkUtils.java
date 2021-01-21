/*
 * MIT License
 *
 * Copyright (c) 2021 BankoBot Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.nycode.bankobot.docdex;

import com.vladsch.flexmark.html2md.converter.HtmlMarkdownWriter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeConverterContext;
import com.vladsch.flexmark.html2md.converter.internal.HtmlConverterCoreNodeRenderer;
import com.vladsch.flexmark.util.misc.Utils;
import com.vladsch.flexmark.util.sequence.LineAppendable;
import com.vladsch.flexmark.util.sequence.RepeatedSequence;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Modifications of {@link HtmlConverterCoreNodeRenderer} to make it work better with javadoc HTML.
 */
public class FlexmarkUtils {

  /**
   * Modification of {@link HtmlConverterCoreNodeRenderer#processPre(Element,
   * HtmlNodeConverterContext, HtmlMarkdownWriter)} which makes all codeblock java.
   * It also removes some weird new lines that are not needed
   */
  @SuppressWarnings("JavadocReference")
  static void processPre(Element element, HtmlNodeConverterContext context,
      HtmlMarkdownWriter out) {
    context.pushState(element);

    String text;
    boolean hadCode = false;
    String className = "";

    HtmlNodeConverterContext preText = context.getSubContext();
    preText.getMarkdown().setOptions(out.getOptions() & ~(LineAppendable.F_COLLAPSE_WHITESPACE
        | LineAppendable.F_TRIM_TRAILING_WHITESPACE));
    preText.getMarkdown().openPreFormatted(false);

    Node next;
    while ((next = context.next()) != null) {
      if (next.nodeName().equalsIgnoreCase("code") || next.nodeName().equalsIgnoreCase("tt")) {
        hadCode = true;
        Element code = (Element) next;
        //text = code.toString();
        preText.renderChildren(code, false, null);
        if (className.isEmpty()) {
          className = Utils
              .removePrefix(code.className(), "language-");
        }
      } else if (next.nodeName().equalsIgnoreCase("br")) {
        preText.getMarkdown().append("\n");
      } else if (next.nodeName().equalsIgnoreCase("#text")) {
        preText.getMarkdown().append(((TextNode) next).getWholeText());
      } else {
        preText.renderChildren(next, false, null);
      }
    }

    preText.getMarkdown().closePreFormatted();
    text = preText.getMarkdown().toString(Integer.MAX_VALUE, 2);

    //int start = text.indexOf('>');
    //int end = text.lastIndexOf('<');
    //text = text.substring(start + 1, end);
    //text = Escaping.unescapeHtml(text);

    int backTickCount = HtmlConverterCoreNodeRenderer.getMaxRepeatedChars(text, '`', 3);
    CharSequence backTicks = RepeatedSequence.repeatOf("`", backTickCount);

    if ((!className.isEmpty() || text.trim().isEmpty() || !hadCode)) {
      out.append(backTicks).append("java").append("\n");
      if (!className.isEmpty()) {
        out.append(className);
      }
      out.line();
      out.openPreFormatted(true);
      if (!text.isBlank() && !text.equals("\n")) {
        // Javadcoc is stupid and adds two new lines at the end of each codeblock which just looks
        // odd in discord so we're going to get rid of those
        if (text.endsWith("\n \n")) {
          out.append(text.substring(0, text.indexOf("\n \n")));
        } else {
          out.append(text);
        }
      }
      out.closePreFormatted();
      out.append(backTicks);
    } else {
      // we indent the whole thing by 4 spaces
      out.pushPrefix();
      out.openPreFormatted(true);
      if (!text.isBlank()) {
        out.append(text);
      }
      out.closePreFormatted();
      out.line();
      out.popPrefix();
    }

    context.popState(out);
  }
}
