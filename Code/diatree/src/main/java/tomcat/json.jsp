<!doctype html></html>
<%  out.print(request.getAttribute("json"));  %>
<meta charset="utf-8" />
<style>
.node circle {     
  fill: #fff;    
  stroke: steelblue;    
  stroke-width: 1.5px; 
} 
.node {    
  font: 20px sans-serif; 
} 
.link {    
  fill: none;    
  stroke: #ccc;    
  stroke-width: 1.5px; 
}
</style> 
<script type="text/javascript" src="http://d3js.org/d3.v3.min.js"></script>
<script type="text/javascript"> 
var width = 600; 
var height = 500; 
var cluster = d3.layout.cluster()    
   .size([height, width-200]); 
var diagonal = d3.svg.diagonal()    
   .projection (function(d) { return [d.y, d.x];}); 
var svg = d3.select("body").append("svg")    
   .attr("width",width)    
   .attr("height",height)    
   .append("g")    
   .attr("transform","translate(100,0)"); 
   
  root = JSON.parse('<%  out.print(request.getAttribute("json"));  %> ');
  var nodes = cluster.nodes(root);    
  var links = cluster.links(nodes);    
  var link = svg.selectAll(".link")       
     .data(links)       
     .enter().append("path")       
     .attr("class","link")       
     .attr("d", diagonal);     
  var node = svg.selectAll(".node")       
     .data(nodes)       
     .enter().append("g")       
     .attr("class","node")       
     .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });    
  node.append("circle")       
     .attr("r", 4.5);    
  node.append("text")       
     .attr("dx", function(d) { return d.children ? -8 : 8; })       
     .attr("dy", 3)       
     .style("text-anchor", function(d) { return d.children ? "end" : "start"; })      
     .text( function(d){ return d.name;}); 
   
</script>