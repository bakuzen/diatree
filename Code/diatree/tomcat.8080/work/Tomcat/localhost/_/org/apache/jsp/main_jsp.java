/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/7.0.50
 * Generated at: 2016-04-01 13:28:12 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class main_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("<!doctype html></html>\n");
      out.write("<meta charset=\"utf-8\" />\n");
      out.write("<style>\n");
      out.write(".node circle {     \n");
      out.write("  fill: #fff;    \n");
      out.write("  stroke: steelblue;    \n");
      out.write("  stroke-width: 1.5px; \n");
      out.write("} \n");
      out.write(".node {    \n");
      out.write("  font: 20px sans-serif; \n");
      out.write("} \n");
      out.write(".link {    \n");
      out.write("  fill: none;    \n");
      out.write("  stroke: #ccc;    \n");
      out.write("  stroke-width: 1.5px; \n");
      out.write("}\n");
      out.write("</style> \n");
      out.write("<script type=\"text/javascript\" src=\"http://d3js.org/d3.v3.min.js\"></script>\n");
      out.write("<script type=\"text/javascript\"> \n");
      out.write("var width = 600; \n");
      out.write("var height = 500; \n");
      out.write("var cluster = d3.layout.cluster()    \n");
      out.write("   .size([height, width-200]); \n");
      out.write("var diagonal = d3.svg.diagonal()    \n");
      out.write("   .projection (function(d) { return [d.y, d.x];}); \n");
      out.write("var svg = d3.select(\"body\").append(\"svg\")    \n");
      out.write("   .attr(\"width\",width)    \n");
      out.write("   .attr(\"height\",height)    \n");
      out.write("   .append(\"g\")    \n");
      out.write("   .attr(\"transform\",\"translate(100,0)\"); \n");
      out.write("d3.json(\"js/dendrogram01.json\", function(error, root){    \n");
      out.write("   var nodes = cluster.nodes(root);    \n");
      out.write("   var links = cluster.links(nodes);    \n");
      out.write("   var link = svg.selectAll(\".link\")       \n");
      out.write("      .data(links)       \n");
      out.write("      .enter().append(\"path\")       \n");
      out.write("      .attr(\"class\",\"link\")       \n");
      out.write("      .attr(\"d\", diagonal);     \n");
      out.write("   var node = svg.selectAll(\".node\")       \n");
      out.write("      .data(nodes)       \n");
      out.write("      .enter().append(\"g\")       \n");
      out.write("      .attr(\"class\",\"node\")       \n");
      out.write("      .attr(\"transform\", function(d) { return \"translate(\" + d.y + \",\" + d.x + \")\"; });    \n");
      out.write("   node.append(\"circle\")       \n");
      out.write("      .attr(\"r\", 4.5);    \n");
      out.write("   node.append(\"text\")       \n");
      out.write("      .attr(\"dx\", function(d) { return d.children ? -8 : 8; })       \n");
      out.write("      .attr(\"dy\", 3)       \n");
      out.write("      .style(\"text-anchor\", function(d) { return d.children ? \"end\" : \"start\"; })      \n");
      out.write("      .text( function(d){ return d.name;}); \n");
      out.write("}); \n");
      out.write("</script>");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
