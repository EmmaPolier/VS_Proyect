export function renderGraph(graph, container){
  // prepare vis nodes/edges
  const nodes = graph.nodes.map(n=>({id:n.id, label:n.label, color:colorForState(n.state)}));
  const edges = graph.edges.map(e=>({from:e.from, to:e.to}));

  const data = {nodes:new vis.DataSet(nodes), edges:new vis.DataSet(edges)};
  const options = {physics:{stabilization:true}, nodes:{font:{color:'#ffffff'}}};

  // clear container
  container.innerHTML = '';
  const network = new vis.Network(container, data, options);

  network.on('selectNode', function(params){
    const id = params.nodes[0];
    console.log('nodo seleccionado', id);
  });
}

function colorForState(s){
  switch((s||'S')){
    case 'I': return '#ef4444';
    case 'R': return '#60a5fa';
    default: return '#94a3b8';
  }
}
