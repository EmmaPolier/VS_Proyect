// apiMock.js
// Mock API layer used by frontend during development.
export async function getGraph(){
	const res = await fetch('mock/graph.json');
	return res.json();
}

export async function getNodes(){
	const g = await getGraph();
	return g.nodes;
}

export async function getSimSteps(){
	// optional: provide mock steps from a static file if added
	try{
		const res = await fetch('mock/steps.json');
		if(!res.ok) throw new Error('no steps');
		return res.json();
	}catch(e){
		return [];
	}
}

export async function putNode(id, payload){
	// in mock, just resolve and return merged object
	return {id, ...payload};
}

export async function postExecuteSim(params){
	// simulate execution and return a fake sim id or steps
	return {simId: Math.floor(Math.random()*10000), status:'ok'};
}
