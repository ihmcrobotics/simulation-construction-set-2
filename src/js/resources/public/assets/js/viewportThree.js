var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/viewport");

webSocket.onopen = function (e) {
	alert("[open] Connection established");
	alert("Sending to server");
}

webSocket.onclose = function (event) {
	if (event.wasClean) {
		alert('[close] Connection closed cleanly, code=${event.code} reason=${event.reason}');
	} else {
		// e.g. server process killed or network down
		// event.code is usually 1006 in this case
		alert('[close] Connection died code=' + event.code);
	}
}

var canvas = document.getElementById("mainViewportCanvas");

var scene = new THREE.Scene();
var camera = new THREE.PerspectiveCamera(75, canvas.width / canvas.height, 0.1, 1000.0);
camera.position.z = 5;
camera.updateProjectionMatrix();

var renderer = new THREE.WebGLRenderer({ antialiasing: true, canvas: canvas });
renderer.setClearColor("#e5e5e5");
renderer.setSize(canvas.width, canvas.height);

window.addEventListener("resize", () => {
	renderer.setSize(canvas.width, canvas.height);
	camera.aspect = canvas.width / canvas.height;
	camera.updateProjectionMatrix();
});

var raycaster = new THREE.Raycaster();
var mouse = new THREE.Vector2();

renderer.render(scene, camera);

var geometry = new THREE.BoxGeometry(1, 1, 1);

meshX = -10;
for (let i = 0; i < 15; i++) {
	var material = new THREE.MeshLambertMaterial({ color: 0xf7f7f7 });
	var mesh = new THREE.Mesh(geometry, material);
	mesh.position.x = (Math.random() - 0.5) * 10;
	mesh.position.y = (Math.random() - 0.5) * 10;
	mesh.position.z = (Math.random() - 0.5) * 10;
	scene.add(mesh);
}

var light = new THREE.PointLight(0xffffff, 1, 1000);
light.position.set(0, 0, 0);
scene.add(light);

var light = new THREE.PointLight(0xffffff, 2, 1000);
light.position.set(0, 0, 25);
scene.add(light);

var render = function () {
	requestAnimationFrame(render);

	renderer.render(scene, camera);
};

function onMouseMove(event) {
	event.preventDefault();

	var localPosition = getMousePosition(canvas, event);
	mouse.x = (localPosition.x / canvas.width) * 2 - 1;
	mouse.y = -(localPosition.y / canvas.height) * 2 + 1;

	raycaster.setFromCamera(mouse, camera);

	var intersects = raycaster.intersectObjects(scene.children, true);
	for (var i = 0; i < intersects.length; i++) {
		var target = intersects[i].object;
		target.material.color.set(0xffff00);
		this.tl = new TimelineMax().delay(0.3);
		this.tl.to(target.scale, 1, { x: 2, ease: Expo.easeOut });
		this.tl.to(target.scale, 0.5, { x: 0.5, ease: Expo.easeOut });
		this.tl.to(target.position, 0.5, { x: 2.0, ease: Expo.easeOut });

		if (webSocket.OPEN) {
			console.info("Websocket open! Sending message!")
			webSocket.send("Yoohoo!");
		}
	}
}

function getMousePosition(canvas, event) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: event.clientX - rect.left,
		y: event.clientY - rect.top,
	};
}

render();

canvas.addEventListener("click", onMouseMove);

function newSphereFunction() {
	var geometry = new THREE.SphereGeometry(0.5, 32, 32);
	var material = new THREE.MeshLambertMaterial({ color: "lightgreen" });
	var mesh = new THREE.Mesh(geometry, material);
	mesh.position.x = (Math.random() - 0.5) * 10;
	mesh.position.y = (Math.random() - 0.5) * 10;
	mesh.position.z = (Math.random() - 0.5) * 10;
	scene.add(mesh);
}

var meshMap = new Map();

// webSocket.onmessage = function newMesh(message) {
// 	var protoMesh = Mesh.deserializeBinary(message.data);
// 	var mesh;

// 	if (!meshMap.has(protoMesh.getMeshId())) {
// 		var geometry;
// 		if (protoMesh.hasBoxGeometry()) {
// 			var protoGeometry = protoMesh.getBoxGeometry();
// 			geometry = new THREE.BoxGeometry(protoGeometry.getSizeX(), protoGeometry.getSizeY(), protoGeometry.getZ());
// 		}
// 		else if (protoMesh.hasConeGeometry()) {
// 			var protoGeometry = protoMesh.getConeGeometry();
// 			geometry = new THREE.ConeGeometry(protoGeometry.getRadius(), protoGeometry.getHeight(), protoGeometry.getRadialSegments());
// 		}
// 		else if (protoMesh.hasCylinderGeometry()) {
// 			var protoGeometry = protoMesh.getCylinderGeometry();
// 			geometry = new THREE.CylinderGeometry(protoGeometry.getRadius(), protoGeometry.getHeight(), protoGeometry.getRadialSegments());
// 		}
// 		else if (protoMesh.hasSphereGeometry()) {
// 			var protoGeometry = protoMesh.getSphereGeometry();
// 			geometry = new THREE.SphereGeometry(protoGeometry.getRadius(), protoGeometry.getWidthSegments(), protoGeometry.getHeightSegments());
// 		}
// 		else if (protoMesh.hasModelFileGeometry()) {
// 			var protoGeometry = protoMesh.getModelFileGeometry();
// 			console.error("Unsupport model file.");
// 			return;
// 		}

// 		var material = new THREE.MeshLambertMaterial({ color: protoMesh.getColor().getWebColor() });
// 		mesh = new THREE.Mesh(geometry, material);
// 		scene.add(mesh);
// 		meshMap.set(proto.getMeshId(), mesh);
// 	}
// 	else {
// 		mesh = mesh.get(protoMesh.getMeshId());
// 	}

// 	if (protoMesh.hasPose()) {
// 		var protoPose = protoMesh.getPose();

// 		if (protoPose.hasOrientation()) {
// 			var protoOrientation = protoPose.getOrientation();
// 			mesh.quaternion.x = protoOrientation.getX();
// 			mesh.quaternion.x = protoOrientation.getY();
// 			mesh.quaternion.x = protoOrientation.getZ();
// 			mesh.quaternion.x = protoOrientation.getS();
// 		}

// 		if (protoPose.hasPosition()) {
// 			var protoPosition = protoPose.getPosition();
// 			mesh.position.x = protoPosition.getX();
// 			mesh.position.y = protoPosition.getY();
// 			mesh.position.z = protoPosition.getZ();
// 		}
// 	}
// }
