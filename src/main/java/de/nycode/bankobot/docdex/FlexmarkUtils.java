/*
 *     This file is part of the BankoBot Project.
 *     Copyright (C) 2021  BankoBot Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <https://www.gnu.org/licenses/>.
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
