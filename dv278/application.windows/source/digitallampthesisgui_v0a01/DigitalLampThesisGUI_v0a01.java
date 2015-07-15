package digitallampthesisgui_v0a01;

import java.util.ArrayList;
import processing.core.PApplet;
import peasy.*;
import controlP5.*;
import toxi.geom.*;
import toxi.processing.*;
import toxi.geom.mesh.*;
import toxi.util.datatypes.*;
import toxi.volume.*;
import geomerative.*;
import javax.swing.JFrame;

public class DigitalLampThesisGUI_v0a01 extends PApplet {

    /**
     * This is GUI software of Project of "The Digital Lamp of Architecture"
     * Written by Gene Ting-Chun Kao +GENEATCG http://www.geneatcg.com all right
     * reserve. Code credit to Jared Counts’ Curtain Coding Structure in
     * BlueThen.com
     */
    private static final long serialVersionUID = 1L;

    PeasyCam cam, cam2;

    ControlP5 cp5;
    ControlWindow fcp;
    Textarea ParticleData;
    Group g1, g2, g3, g4, g5;
    Accordion accordion;
    RadioButton ParticleForce;
    RadioButton multiLock;
    RadioButton webStitchType;
    RadioButton FileType;
    RadioButton webSwitch;
    RadioButton webSwitchTo;
    RadioButton Export;

    public int stitchID = 2;
    public boolean together = false;

    float StitchLength = 0;
    float Xpan = 0;
    float Ypan = 0;
    float Zpan = 0;
    float Mass = 0.5f;
    float Scale = 1.0f;
    float Xto = 0;
    float Yto = 0;
    float Zto = 0;
    int gravity1 = 392;
    int gravity2 = -392;
    boolean voxel = false;
    boolean modifyPin = true;
    public boolean meshExport = true;
    public boolean record = false;

    String PdataTxt = "";

    boolean toggle = false;
    int normalValue1 = 10;
    int normalValue2 = 10;

    boolean lockedpMulti = true;
    int webStitchDiff = 0; // 0 same, 1 diff, 2 all
    int exState = 0;

    ArrayList<Particle> lockedp;

    Particle selectedp;

    EmbeddedSketch eSketch;
    ChildApplet child = new ChildApplet();
    boolean mousePressedOnParent = false;

    float ISO_THRESHOLD = 3;
    float density = 0.5f;
    int voxelRes = 240;

    ToxiclibsSupport gfx;
    WETriangleMesh mesh, vMesh, fMesh;
    VolumetricSpace volume;
    IsoSurface surface;
    VolumetricBrush brush;
    AABB bounds3D;
    MeshLatticeBuilder builder;

    public boolean pause = false;
    boolean showNormals;

    public int co = color(235, 57, 174, 200);

    Web w, w1, w2, w3, w4, w5, w6;

    public Vec3D c2;
    public Vec3D extent;
    public float ww;

    public void setup() {
        size(600, 700, OPENGL);
        frame.setLocation(800, 100);

        smooth();

        lockedp = new ArrayList<Particle>();
        gui();

        cam = new PeasyCam(this, 1000);
        w = new Web();
        w1 = new Web(this, 25, 25);
        w2 = new Web(this, 25, 25);

        eSketch = new EmbeddedSketch(child);

        mesh = new WETriangleMesh("meshy");
        gfx = new ToxiclibsSupport(this);

        setupVMesh();
        fMesh = new WETriangleMesh("test");
    }

    public void draw() {

        background(200);
        mesh.clear();

        noStroke();

        if (record) {
            beginRaw("superCAD." + "Rhino",
                    "oneDome" + (System.currentTimeMillis() / 1000) + ".rvb");
            w1.draw(gravity1, normalValue1);
            w2.draw(gravity2, normalValue2);
            endRaw();
            record = false;
        } else {
            w1.draw(gravity1, normalValue1);
            w2.draw(gravity2, normalValue2);
        }

        fill(191, 202, 212, 100);

        if (pause && voxel) { // pause seem more intuitive
            drawVMesh();
        }
    }

    public void keyPressed() {
        if (key == RETURN || key == ENTER) {
            pause = !pause;
        }

        if (key == 'e') {
            vMesh.saveAsSTL(sketchPath(mesh.name
                    + (System.currentTimeMillis() / 1000) + ".stl"));
        }
        if (key == 'm') {
            record = true;
        }
        if (key == 'c') {
            volume.clear();
        }
        if (key == 'l')
            new LaplacianSmooth().filter(fMesh, 1);

        if (key == 'n') {
            showNormals = !showNormals;
        }

    }

    private void setupVMesh() {

        bounds3D = new AABB();
        float ww = w1.webWidth * w1.restingDistances;
        float wh = w1.webHeight * w1.restingDistances;

        Vec3D c1 = new Vec3D(ww / 2, ww / 2, ww * 2);
        Vec3D c2 = new Vec3D(-wh / 2, -wh / 2, -wh * 2);
        bounds3D.growToContainPoint(c1);
        bounds3D.growToContainPoint(c2);
        extent = bounds3D.getExtent();

        float maxAxis = max(extent.x, extent.y, extent.z);
        int resX = (int) (extent.x / maxAxis * voxelRes);
        int resY = (int) (extent.y / maxAxis * voxelRes);
        int resZ = (int) (extent.z / maxAxis * voxelRes);
        builder = new MeshLatticeBuilder(extent.scale(2), resX, resY, resZ,
                new FloatRange(1, 1));
        builder.setInputBounds(new AABB(bounds3D, extent.scale(1)));
        volume = builder.getVolume();
        surface = new ArrayIsoSurface(volume);
        brush = new BoxBrush(volume, 0.20f);
        brush.setMode(VolumetricBrush.MODE_PEAK); // //////////////
        vMesh = new WETriangleMesh("dome");

    }

    private void drawVMesh() {

        volume.closeSides();
        new HashIsoSurface(volume).computeSurfaceMesh(vMesh, 1f); //

        new LaplacianSmooth().filter(vMesh, 2);
        gfx.mesh(vMesh, true, showNormals ? 10 : 0);
        exportVMesh();
    }

    private void exportVMesh() {
        if (meshExport) {
            vMesh.saveAsSTL(sketchPath(vMesh.name
                    + (System.currentTimeMillis() / 1000) + ".stl"));
        }
        meshExport = false;
    }

    // //////////////////////////////////////////////////////////////////////
    public static void main(String _args[]) {
        PApplet.main(new String[] { digitallampthesisgui_v0a01.DigitalLampThesisGUI_v0a01.class
                .getName() });
    }

    // //////////////////////////////////////////////////////////////////////

    public class EmbeddedSketch extends JFrame {
        /**
         * this is GUI top view
         */
        private static final long serialVersionUID = 1L;
        PApplet sketch;

        public EmbeddedSketch(PApplet p) {
            setTitle("plane");
            setBounds(0, 0, 600, 720);
            setLocation(200, 100);
            setResizable(false);
            add(p);
            p.init();
            sketch = p;
            // Program exits
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }
    }

    public class ChildApplet extends PApplet {

        /**
         * This Class is GUI Top View and GUI Slider
         */
        private static final long serialVersionUID = 1L;
        RGeomElem mo;
        Vec3D mouseP;

        public void setup() {
            size(600, 700, OPENGL);
            smooth();

            RG.init(this);
            cam2 = new PeasyCam(this, 0, 0, 0, 550);
            cam2.setRollRotationMode();
            cam2.setLeftDragHandler(null);
            cam2.setRightDragHandler(null);
            cam2.reset();
            w = w1;
        }

        public void draw() {
            background(200);

            w.drawApplet(this);

            fill(255, 255, 0);
            mouseP = Mouse_Applet(this, mouseX, mouseY);
            mo = RShape.createCircle(mouseP.x, mouseP.y, 5).toShape();
            mo.draw(child);

            interaction();
        }

        public void interaction() {
            for (Particle p : w.particles) {
                try {
                    if (p.rge.toShape().intersects(mo)) {

                        if (p.getpState() != 2 && p.getpState() != 3)
                            p.setpState(1);

                        selectedp = p;
                        if (mousePressed && mouseButton == LEFT) {
                            p.position = new Vec3D(mouseP.x, mouseP.y,
                                    p.position.z);
                        }
                        PdataTxt = "Cooridernate: "
                                + round(selectedp.position.x) + ","
                                + round(selectedp.position.y) + ","
                                + round(selectedp.position.z) + '\n'
                                + "Statue: " + selectedp.getpState()
                                + "  Pinned: " + selectedp.isPinned() + '\n'
                                + "Mass: " + selectedp.mass + "  ID = "
                                + selectedp.getUV()[0] + ", "
                                + selectedp.getUV()[1] + '\n'
                                + "Force Driection: " + round(selectedp.fg.x)
                                + "," + round(selectedp.fg.y) + ","
                                + round(selectedp.fg.z);
                        ParticleData.setText(PdataTxt);

                    } else {
                        if (p.getpState() != 2 && p.getpState() != 3)
                            p.setpState(0);
                    }
                } catch (NullPointerException e) {
                    println("catch, What the fuck! Why Stop?");
                }
            }
        }

        private Vec3D Mouse_Applet(ChildApplet a, float x, float y) {
            Vec3D mouse = new Vec3D(x - a.width / 2, y - a.height / 2, 0);
            Vec3D mousePV = new Vec3D(20, 0, 0);
            Vec3D screenPV = new Vec3D(a.g.screenX(20, 0) - a.g.screenX(0, 0),
                    0, 0);
            float rMS = mousePV.magnitude() / screenPV.magnitude();
            Vec3D mouseNew = mouse.copy();
            mouseNew.scaleSelf(rMS);
            mouseNew.addSelf(new Vec3D(cam2.getLookAt()[0],
                    cam2.getLookAt()[1], 0));
            return mouseNew;
        }

        public void keyPressed() {
            if (key == RETURN || key == ENTER) {
                pause = !pause;
            }

            if (key == 'e') {
                vMesh.saveAsSTL(sketchPath(mesh.name
                        + (System.currentTimeMillis() / 1000) + ".stl"));
            }
            if (key == 'c') {
                volume.clear();
            }
            if (key == 'r') {
                if (selectedp.links.size() > 1)
                    selectedp.links.remove(selectedp.links.size() - 1);
            }
            if (key == 't') {
                together = true;
                selectedp.together();
            }
            if (key == CODED) {
                if (keyCode == SHIFT) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp
                                .pinTo(new Vec3D(selectedp.position.x,
                                        selectedp.position.y,
                                        selectedp.position.z + 2));
                    else
                        selectedp.position.add(new Vec3D(0, 0, 1));
                } else if (keyCode == ALT) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp
                                .pinTo(new Vec3D(selectedp.position.x,
                                        selectedp.position.y,
                                        selectedp.position.z - 2));
                    else
                        selectedp.position.add(new Vec3D(0, 0, -1));
                } else if (keyCode == RIGHT) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp.pinTo(new Vec3D(selectedp.position.x + 2,
                                selectedp.position.y, selectedp.position.z));
                    else
                        selectedp.position.add(new Vec3D(-1, 0, 0));
                } else if (keyCode == LEFT) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp.pinTo(new Vec3D(selectedp.position.x - 2,
                                selectedp.position.y, selectedp.position.z));
                    else
                        selectedp.position.add(new Vec3D(1, 0, 0));
                } else if (keyCode == DOWN) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp
                                .pinTo(new Vec3D(selectedp.position.x,
                                        selectedp.position.y + 2,
                                        selectedp.position.z));
                    else
                        selectedp.position.add(new Vec3D(0, 1, 0));
                } else if (keyCode == UP) {
                    if (selectedp.getpState() == 2
                            || selectedp.getpState() == 3)
                        selectedp
                                .pinTo(new Vec3D(selectedp.position.x,
                                        selectedp.position.y - 2,
                                        selectedp.position.z));
                    else
                        selectedp.position.add(new Vec3D(0, -1, 0));
                }
            }
            if (key == 'p') {
                if (selectedp != null) {
                    if (selectedp.isPinned() || selectedp.getpState() == 2) {
                        selectedp.setpState(0);
                        selectedp.setPinned(false);
                    } else if (selectedp.getpState() == 3) {
                        if (selectedp.isPinned() == false)
                            selectedp.pinTo(new Vec3D(mouseP.x, mouseP.y,
                                    selectedp.position.z));
                        else {
                            selectedp.setPinned(false);
                            selectedp.setpState(3);
                        }

                    } else {
                        selectedp.pinTo(new Vec3D(mouseP.x, mouseP.y,
                                selectedp.position.z));
                        selectedp.setpState(2);
                    }
                }
            }

            if (key == 'l') {
                if (lockedpMulti) { // multi points
                    if (lockedp.isEmpty()
                            && (selectedp.getpState() == 1 || selectedp
                                    .getpState() == 2)) {
                        selectedp.setpState(3);
                        lockedp.add(selectedp);
                    } else if (selectedp.getpState() == 1
                            || selectedp.getpState() == 3
                            || selectedp.getpState() == 2) {
                        if (lockedp.contains(selectedp)) {
                            if (selectedp.isPinned()) {
                                lockedp.remove(selectedp);
                                selectedp.setpState(2);
                                selectedp.setPinned(true);
                            } else {
                                lockedp.remove(selectedp);
                                selectedp.setpState(0);
                                selectedp.setPinned(false);
                            }
                        } else {
                            selectedp.setpState(3);
                            lockedp.add(selectedp);
                        }
                    }
                    println(lockedp.size());
                } else if (!lockedpMulti) { // single point
                    if (lockedp.isEmpty()
                            && (selectedp.getpState() == 1 || selectedp
                                    .getpState() == 2)) {
                        lockedp.add(selectedp);
                        selectedp.setpState(3);
                    } else if (lockedp.isEmpty() && selectedp.getpState() != 1) {
                        println("select nothing");
                    } else if (lockedp.size() == 1
                            && selectedp.getpState() != 1) {
                        lockedp.get(0).setpState(0);
                        lockedp.clear();
                    } else if (lockedp.size() == 1
                            && selectedp.getpState() == 1) {
                        if (lockedp.get(0) == selectedp) {
                            lockedp.get(0).setpState(1);
                            lockedp.clear();
                        } else {
                            lockedp.get(0).setpState(1);
                            lockedp.clear();
                            lockedp.add(selectedp);
                        }
                    }
                }
            }

            lockedpanel();
        }

        public void lockedpanel() {
            boolean b;
            b = true;

            selectedSliderActive(cp5, "StitchLength", b, g2);
            selectedSliderActive(cp5, "Xpan", b, g3);
            selectedSliderActive(cp5, "Ypan", b, g3);
            selectedSliderActive(cp5, "Zpan", b, g3);
            selectedSliderActive(cp5, "Mass", b, g3);
            selectedSliderActive(cp5, "Scale", b, g3);
            selectedSliderActive(cp5, "Xto", b, g3);
            selectedSliderActive(cp5, "Yto", b, g3);
            selectedSliderActive(cp5, "Zto", b, g3);
            selectedBangActive(cp5, "Apply", b, g3);
            selectedBangActive(cp5, "MoveTo", b, g3);
            selectedBangActive(cp5, "ScaleA", b, g3);
            selectedBangActive(cp5, "Stitch", b, g2);
            selectedBangActive(cp5, "Clear", b, g2);
            selectedSliderActive(cp5, "gravity1", b, g4);
            selectedSliderActive(cp5, "gravity2", b, g4);
            selectedToggleActive(cp5, "voxel", b, g4);
            selectedBangActive(cp5, "Export", b, g4);
        }

        public void selectedSliderActive(ControlP5 cp, String n, boolean b,
                Group g) {

            Slider s = (Slider) cp.getController(n);
            // general slider color setting
            if (b) {
                s.unlock().setGroup(g);
            } else {
                s.setColorForeground(color(255, 150)).setValue(0).lock()
                        .setGroup(g);
            }
        }

        public void selectedBangActive(ControlP5 cp, String n, boolean b,
                Group g) {
            Bang ba = (Bang) cp.getController(n);
            // general slider color setting
            if (b) {
                ba.unlock().setGroup(g);
            } else if (n == ba.getName()) {
                ba.setColorForeground(color(255, 150)).setValue(0).lock()
                        .setGroup(g);
            }
        }

        public void selectedToggleActive(ControlP5 cp, String n, boolean b,
                Group g) {
            Toggle ba = (Toggle) cp.getController(n);
            // general slider color setting
            if (b) {
                ba.unlock().setGroup(g);
            } else if (n == ba.getName()) {
                ba.setColorForeground(color(255, 150)).setValue(0).lock()
                        .setGroup(g);
            }
        }

        public void Stitch() {
            if (!lockedp.isEmpty()) {
                Vec3D np = new Vec3D();

                if (webStitchDiff == 1) { // DiffWeb
                    for (Particle p : lockedp) {
                        Particle wp = w2.getUV(p.getUV()[0], p.getUV()[1]);
                        if (wp != null) {
                            if (stitchID == 1)
                                wp = w1.getUV(p.getUV()[0], p.getUV()[1]);
                            else if (stitchID == 2)
                                wp = w2.getUV(p.getUV()[0], p.getUV()[1]);
                            else if (stitchID == 3)
                                wp = w3.getUV(p.getUV()[0], p.getUV()[1]);
                            else if (stitchID == 4)
                                wp = w4.getUV(p.getUV()[0], p.getUV()[1]);
                            else if (stitchID == 5)
                                wp = w5.getUV(p.getUV()[0], p.getUV()[1]);
                            else if (stitchID == 6)
                                wp = w6.getUV(p.getUV()[0], p.getUV()[1]);
                            // np = p.position.add(wp.position);
                            // np.scale(0.5f);
                            // p.pinTo(np);
                            p.attachTo(wp, StitchLength, 1);
                            p.setpState(0);
                            p.setPinned(false);
                        }
                    }
                } else if (webStitchDiff == 0) { // same
                    for (Particle p : lockedp) {
                        np = np.addSelf(p.position);
                    }
                    np.scaleSelf(1.0f / lockedp.size());
                    lockedp.get(0).pinTo(np);
                    int id = 0;
                    for (Particle p : lockedp) {
                        if (id != 0)
                            p.attachTo(lockedp.get(0), StitchLength, 1);
                        p.setpState(0);
                        id++;
                    }
                    lockedp.get(0).setPinned(false);
                } else if (webStitchDiff == 2) { // all Kind
                    if (lockedp.size() > 1)
                        for (int i = 1; i < lockedp.size(); i++) {
                            Particle p = lockedp.get(i - 1);
                            Particle q = lockedp.get(i);
                            p.attachTo(q, StitchLength, 1);
                            p.setpState(0);
                            p.setPinned(false);
                            q.setpState(0);
                            q.setPinned(false);
                        }
                }
                lockedp.clear();
            }
        }

        public void Clear() {
            if (!lockedp.isEmpty()) {
                lockedp.clear();
            }
            println("lockedp clear!");
        }

        public void Apply() {
            if (!lockedp.isEmpty()) {

                for (Particle p : lockedp) {
                    p.pinTo(new Vec3D(p.position.x + Xpan, p.position.y + Ypan,
                            p.position.z + Zpan));
                    p.setpState(3);
                    p.setMass(Mass);
                }
            }

        }

        public void MoveTo() {
            if (!lockedp.isEmpty()) {

                for (Particle p : lockedp) {
                    p.pinTo(new Vec3D(Xto, Yto, Zto));
                    p.setpState(3);

                    p.setMass(Mass);
                }
            }
        }

        public void ScaleA() {
            if (!lockedp.isEmpty()) {
                Vec3D sum = new Vec3D();

                for (Particle p : lockedp) {
                    sum.addSelf(p.position);
                }
                sum.scale(1.0f / lockedp.size());
                println(sum);
                for (Particle p : lockedp) {

                    Vec3D newP = new Vec3D(p.position.add((sum.sub(p.position))
                            .scale(Scale)));
                    Vec3D newPXY = new Vec3D(newP.x, newP.y, 0);
                    p.pinTo(newPXY);
                    p.setpState(3);
                }
            }
        }

        public void Export() {
            meshExport = true;
        }

    }

    public void gui() {
        cp5 = new ControlP5(this);
        fcp = cp5.addControlWindow("panel", 0, 100, 200, 700);
        fcp.setBackground(200);

        // g1
        g1 = cp5.addGroup("Particle Data").setBackgroundColor(color(50, 64))
                .moveTo(fcp);

        ParticleData = cp5.addTextarea("ParticleData").setPosition(5, 5)
                .setWidth(170).setLineHeight(12).setGroup(g1);

        ParticleForce = cp5.addRadioButton("ParticleForce").setPosition(5, 75)
                .setItemWidth(15).setItemHeight(15).setItemsPerRow(3)
                .setSpacingColumn(40).addItem("Gravity", 0)
                .addItem("Center", 1).addItem("Specific", 2)
                .setNoneSelectedAllowed(false).activate(0).setGroup(g1);

        multiLock = cp5.addRadioButton("multiLock").setPosition(5, 55)
                .setItemWidth(15).setItemHeight(15).setItemsPerRow(3)
                .setSpacingColumn(40).addItem("Single", 0)
                .addItem("MultiPoint", 1).setNoneSelectedAllowed(false)
                .activate(0).setGroup(g1);

        // g2
        g2 = cp5.addGroup("StitchParameter").setBackgroundColor(color(50, 64))
                .setBackgroundHeight(170).moveTo(fcp);

        webStitchType = cp5.addRadioButton("webStitchType").setPosition(5, 5)
                .setItemWidth(15).setItemHeight(15).setItemsPerRow(3)
                .setSpacingColumn(40).addItem("Same", 0).addItem("Diff", 1)
                .addItem("All Kind", 2).setNoneSelectedAllowed(false)
                .activate(0).setGroup(g2);

        cp5.addBang("Stitch").setPosition(5, 110).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "Stitch")
                .setGroup(g2);
        cp5.addBang("Clear").setPosition(60, 110).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "Clear")
                .setGroup(g2);

        webSwitch = cp5.addRadioButton("webSwitch").setPosition(5, 30)
                .setItemWidth(15).setItemHeight(15).setItemsPerRow(3)
                .setSpacingColumn(40).addItem("Web1", 0).addItem("Web2", 1)
                .setNoneSelectedAllowed(false).activate(0).setGroup(g2);
        webSwitchTo = cp5.addRadioButton("webSwitchTo").setPosition(5, 70)
                .setItemWidth(15).setItemHeight(15).setItemsPerRow(3)
                .setSpacingColumn(40).addItem("WebTo1", 0).addItem("WebTo2", 1)
                .setNoneSelectedAllowed(false).activate(0).setGroup(g2);
        cp5.addSlider("StitchLength").setPosition(5, 145).setSize(100, 15)
                .setRange(0, 500).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g2);

        // g3
        g3 = cp5.addGroup("ParticleParameter").setWidth(180)
                .setBackgroundHeight(200).setBackgroundColor(color(50, 64))
                .moveTo(fcp);

        cp5.addSlider("Xpan").setPosition(5, 5).setSize(100, 15)
                .setRange(-10, 10).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Ypan").setPosition(5, 25).setSize(100, 15)
                .setRange(-10, 10).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Zpan").setPosition(5, 45).setSize(100, 15)
                .setRange(-10, 10).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Mass").setPosition(5, 65).setSize(100, 15)
                .setRange(-10, 10).setValue(0.5f)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Scale").setPosition(5, 95).setSize(100, 15)
                .setRange(0.5f, 2).setValue(1.0f)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Xto").setPosition(5, 125).setSize(100, 15)
                .setRange(-300, 300).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Yto").setPosition(5, 145).setSize(100, 15)
                .setRange(-300, 300).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        cp5.addSlider("Zto").setPosition(5, 165).setSize(100, 15)
                .setRange(-1000, 1000).setValue(0)
                .setColorForeground(color(100, 150)).setGroup(g3);
        // bang
        cp5.addBang("Apply").setPosition(145, 5).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "Apply")
                .setGroup(g3);
        cp5.addBang("MoveTo").setPosition(145, 125).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "MoveTo")
                .setGroup(g3);
        cp5.addBang("ScaleA").setPosition(145, 95).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "ScaleA")
                .setGroup(g3);

        // g4
        g4 = cp5.addGroup("GravityTest").setBackgroundColor(color(50, 64))
                .moveTo(fcp);

        cp5.addSlider("gravity1").setPosition(5, 5).setSize(100, 15)
                .setRange(-392, 392).setValue(392).setGroup(g4);
        cp5.addSlider("gravity2").setPosition(5, 25).setSize(100, 15)
                .setRange(-392, 392).setValue(-392).setGroup(g4);

        cp5.addToggle("voxel").setPosition(5, 55).setSize(15, 15)
                .setValue(false).setColorForeground(color(100, 150))
                .setGroup(g4).unlock();
        cp5.addBang("Export").setPosition(45, 55).setSize(15, 15)
                .setColorForeground(color(100, 150)).plugTo(child, "Export")
                .setGroup(g4);

        // g5
        g5 = cp5.addGroup("GravityApply").setWidth(180).setPosition(10, 630)
                .setBackgroundColor(color(50, 64)).moveTo(fcp);

        cp5.addToggle("toggle").setPosition(5, 35).setSize(15, 15)
                .setColorForeground(color(100, 150)).setGroup(g5).unlock();

        cp5.addSlider("normalValue1").setPosition(5, 5).setSize(100, 10)
                .setRange(-100, 100).setColorForeground(color(100, 150))
                .setGroup(g5);
        cp5.addSlider("normalValue2").setPosition(5, 20).setSize(100, 10)
                .setRange(-100, 100).setColorForeground(color(100, 150))
                .setGroup(g5);

        // groups setting
        accordion = cp5.addAccordion("Data").setPosition(10, 20).setWidth(180)
                .addItem(g1).addItem(g2).addItem(g3).addItem(g4);
        accordion.moveTo(fcp).open(0, 1, 2, 3, 4)
                .setCollapseMode(Accordion.MULTI);
        cp5.setAutoDraw(false);

    }

    public int radioPick(ControlEvent ev) {
        int id = 0;
        for (float v : ev.getGroup().getArrayValue()) {
            if (v == 1)
                break;
            id++;
        }
        return id;
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isFrom(ParticleForce)) {
            int id = radioPick(theEvent);
            for (Particle p : w.particles) {
                p.setfState(id);
            }
        } else if (theEvent.isFrom(multiLock)) {
            int id = radioPick(theEvent);
            if (id == 0)
                lockedpMulti = false;
            else if (id == 1)
                lockedpMulti = true;
        } else if (theEvent.isFrom(webStitchType)) {
            int id = radioPick(theEvent);
            if (id == 0)
                webStitchDiff = 0;
            else if (id == 1)
                webStitchDiff = 1;
            else if (id == 2)
                webStitchDiff = 2;
        } else if (theEvent.isFrom(FileType)) {
            int id = radioPick(theEvent);
            if (id == 0)
                exState = 0;
            else if (id == 1)
                exState = 1;
        } else if (theEvent.isFrom(webSwitch)) {
            w = new Web();
            if (w != null) {
                int id = radioPick(theEvent);
                cam2 = new PeasyCam(child, 0, 0, 0, 350);
                cam2.setRollRotationMode();
                cam2.setLeftDragHandler(null);
                cam2.setRightDragHandler(null);
                cam2.reset();

                if (id == 0)
                    w = w1;
                else if (id == 1)
                    w = w2;
            }
        } else if (theEvent.isFrom(webSwitchTo)) {
            int id = radioPick(theEvent);
            if (id == 0)
                stitchID = 1;
            else if (id == 1)
                stitchID = 2;
        }
    }

}
