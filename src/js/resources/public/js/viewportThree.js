const THREE = require("three");
const THREEMessages = require("./protobuf/three_pb");

var webSocket = new WebSocket(
  "ws://" + location.hostname + ":" + location.port + "/viewport"
);

webSocket.onopen = function (e) {
  alert("[open] Connection established");
};

webSocket.onclose = function (event) {
  if (event.wasClean) {
    alert(
      "[close] Connection closed cleanly, code=${event.code} reason=${event.reason}"
    );
  } else {
    // e.g. server process killed or network down
    // event.code is usually 1006 in this case
    alert("[close] Connection died code=" + event.code);
  }
};

var canvas = document.getElementById("mainViewportCanvas");

var scene = new THREE.Scene();
var camera = new THREE.PerspectiveCamera(75, 2, 0.1, 1000.0);
camera.position.z = 5;

var renderer = new THREE.WebGLRenderer({ antialias: true, canvas: canvas });
renderer.setClearColor("#e5e5e5");

var raycaster = new THREE.Raycaster();
var mouse = new THREE.Vector2();

var geometry = new THREE.BoxGeometry(1, 1, 1);

for (let i = 0; i < 15; i++) {
  let material = new THREE.MeshLambertMaterial({ color: 0xf7f7f7 });
  let mesh = new THREE.Mesh(geometry, material);
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

function resizeCanvasToDisplaySize() {
  // look up the size the canvas is being displayed
  const width = canvas.clientWidth;
  const height = canvas.clientHeight;

  // adjust displayBuffer size to match
  if (canvas.width !== width || canvas.height !== height) {
    // you must pass false here or three.js sadly fights the browser
    renderer.setSize(width, height, false);
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
  }
}

function animate(time) {
  resizeCanvasToDisplaySize();

  renderer.render(scene, camera);
  requestAnimationFrame(animate);
}

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
  }
}

function getMousePosition(canvas, event) {
  var rect = canvas.getBoundingClientRect();
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top,
  };
}

requestAnimationFrame(animate);

canvas.addEventListener("click", onMouseMove);

var meshGroupMap = new Map();

webSocket.onmessage = function (event) {
  event.data.arrayBuffer().then((value) => {
    console.info(value);
    var protoMeshGroup = THREEMessages.MeshGroup.deserializeBinary(value);
    console.info(protoMeshGroup);
    var meshGroup;

    if (!meshGroupMap.has(protoMeshGroup.getGroupId())) {
      meshGroup = new THREE.Group();
      protoMeshGroup.getMeshesList().forEach((protoMesh) => {
        meshGroup.add(newProtoMesh(protoMesh));
      });
      scene.add(meshGroup);
    } else {
      meshGroup = meshGroupMap.get(proto.getGroupId());
    }

    if (protoMeshGroup.hasPose()) {
      var protoPose = protoMeshGroup.getPose();

      if (protoPose.hasOrientation()) {
        var protoOrientation = protoPose.getOrientation();
        meshGroup.quaternion.x = protoOrientation.getX();
        meshGroup.quaternion.x = protoOrientation.getY();
        meshGroup.quaternion.x = protoOrientation.getZ();
        meshGroup.quaternion.x = protoOrientation.getS();
      }

      if (protoPose.hasPosition()) {
        var protoPosition = protoPose.getPosition();
        meshGroup.position.x = protoPosition.getX();
        meshGroup.position.y = protoPosition.getY();
        meshGroup.position.z = protoPosition.getZ();
      }
    }
  });
};

function newProtoMesh(protoMesh) {
  console.info(protoMesh);
  var geometry;
  if (protoMesh.hasBoxgeometry()) {
    var protoGeometry = protoMesh.getBoxgeometry();
    geometry = new THREE.BoxGeometry(
      protoGeometry.getSize().getX(),
      protoGeometry.getSize().getY(),
      protoGeometry.getSize().getZ()
    );
  } else if (protoMesh.hasConegeometry()) {
    var protoGeometry = protoMesh.getConegeometry();
    geometry = new THREE.ConeGeometry(
      protoGeometry.getRadius(),
      protoGeometry.getHeight(),
      protoGeometry.getRadialSegments()
    );
  } else if (protoMesh.hasCylindergeometry()) {
    var protoGeometry = protoMesh.getCylindergeometry();
    geometry = new THREE.CylinderGeometry(
      protoGeometry.getRadius(),
      protoGeometry.getHeight(),
      protoGeometry.getRadialSegments()
    );
  } else if (protoMesh.hasSpheregeometry()) {
    var protoGeometry = protoMesh.getSpheregeometry();
    geometry = new THREE.SphereGeometry(
      protoGeometry.getRadius(),
      protoGeometry.getWidthSegments(),
      protoGeometry.getHeightSegments()
    );
  } else if (protoMesh.hasModelfilegeometry()) {
    var protoGeometry = protoMesh.getModelfilegeometry();
    console.error("Unsupport model file.");
    return;
  }

  var material = new THREE.MeshLambertMaterial({
    color: protoMesh.getColor().getWebcolor(),
  });
  mesh = new THREE.Mesh(geometry, material);

  if (protoMesh.hasPose()) {
    var protoPose = protoMesh.getPose();

    if (protoPose.hasOrientation()) {
      var protoOrientation = protoPose.getOrientation();
      mesh.quaternion.x = protoOrientation.getX();
      mesh.quaternion.x = protoOrientation.getY();
      mesh.quaternion.x = protoOrientation.getZ();
      mesh.quaternion.x = protoOrientation.getS();
    }

    if (protoPose.hasPosition()) {
      var protoPosition = protoPose.getPosition();
      mesh.position.x = protoPosition.getX();
      mesh.position.y = protoPosition.getY();
      mesh.position.z = protoPosition.getZ();
    }
  }

  return mesh;
}
