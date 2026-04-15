<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Issue Map — Civic Portal</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<style>
body{background:#f0f4f8;margin:0}
.navbar{background:#1a3c6e!important}.navbar-brand,.navbar-text{color:#fff!important}
#map{height:calc(100vh - 56px);width:100%}
.legend{background:#fff;padding:10px 14px;border-radius:8px;box-shadow:0 1px 8px rgba(0,0,0,.15);font-size:.82rem;line-height:1.8}
.dot{display:inline-block;width:10px;height:10px;border-radius:50%;margin-right:6px}
</style>
</head>
<body>
<nav class="navbar navbar-expand-lg">
  <div class="container">
    <a class="navbar-brand fw-bold" href="#">&#127981; Civic Portal — Public Issue Map</a>
    <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light btn-sm ms-auto">Login</a>
  </div>
</nav>
<div id="map"></div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script>
var map = L.map('map').setView([21.1458, 79.0882], 12); // Nagpur default

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
  attribution:'&copy; OpenStreetMap contributors', maxZoom:18
}).addTo(map);

// Color by status
var statusColors = {
  'OPEN':'#dc3545','ASSIGNED':'#fd7e14',
  'IN_PROGRESS':'#0d6efd','RESOLVED':'#198754','CLOSED':'#6c757d'
};

function makeIcon(color){
  return L.divIcon({
    className:'',
    html:'<div style="width:14px;height:14px;border-radius:50%;background:'+color+';border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,.4)"></div>',
    iconSize:[14,14],iconAnchor:[7,7]
  });
}

// Issues from server
var issues = [
  <c:forEach var="issue" items="${mapIssues}" varStatus="s">
    <c:set var="gps" value="${issue.gpsLocation}"/>
    <c:if test="${not empty gps and fn:contains(gps,',')}">
      {id:${issue.issueId},cat:'<c:out value="${issue.category}"/>',
       status:'<c:out value="${issue.status}"/>',
       desc:'<c:out value="${issue.description}"/>',
       lat:${fn:substringBefore(gps,',')},
       lng:${fn:substringAfter(gps,',')}
      }<c:if test="${!s.last}">,</c:if>
    </c:if>
  </c:forEach>
];

issues.forEach(function(i){
  if(isNaN(i.lat)||isNaN(i.lng)) return;
  var color = statusColors[i.status]||'#888';
  L.marker([i.lat,i.lng],{icon:makeIcon(color)})
   .addTo(map)
   .bindPopup('<b>#'+i.id+' — '+i.cat+'</b><br>'+i.desc+'<br><span style="color:'+color+'">'+i.status+'</span>');
});

// Legend
var legend = L.control({position:'bottomright'});
legend.onAdd = function(){
  var div = L.DomUtil.create('div','legend');
  div.innerHTML='<b>Status</b><br>';
  for(var s in statusColors){
    div.innerHTML+='<span class="dot" style="background:'+statusColors[s]+'"></span>'+s+'<br>';
  }
  return div;
};
legend.addTo(map);
</script>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
</body></html>

