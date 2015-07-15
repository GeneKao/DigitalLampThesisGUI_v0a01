package digitallampthesisgui_v0a01;

import geomerative.RGeomElem;
import geomerative.RShape;

import java.util.ArrayList;
import processing.core.PApplet;
import toxi.geom.Vec3D;

public class Particle {

    DigitalLampThesisGUI_v0a01 p5;

    Vec3D position;
    Vec3D lastPosition;
    Vec3D acceleration;

    float mass = 1.0f;
    float damping = 20.0f;
    Vec3D fg;
    RGeomElem rge;
    float psize = 5;

    private int[] uv = new int[2];

    private int pState = 0; // none = 0; over = 1; pinned = 2; locked = 3;
    private int fState = 0;

    Vec3D fc = new Vec3D();

    private boolean pinned = false;
    private boolean pinnedZ = false;

    Vec3D pinLocation = new Vec3D();

    ArrayList<Link> links = new ArrayList<Link>();

    public Particle(DigitalLampThesisGUI_v0a01 p5, Vec3D pos) {
        this.p5 = p5;
        position = pos.copy();
        lastPosition = pos.copy();
        acceleration = new Vec3D();
    }

    public Particle(DigitalLampThesisGUI_v0a01 p5) {
        this.p5 = p5;
    }

    public void draw() {
        p5.stroke(p5.co);

        if (links.size() > 0) {
            for (int i = 0; i < links.size(); i++) {
                Link currentLink = links.get(i);
                currentLink.draw();
            }
        }
        p5.strokeWeight(5);

        checkpColor(p5);
        p5.strokeWeight(1);
        p5.stroke(74, 84, 94);

        drawMassDiagram();

    }

    public void drawApplet(PApplet a) {
        if (links.size() > 0) {
            for (int i = 0; i < links.size(); i++) {
                Link currentLink = links.get(i);
                currentLink.drawApplet(a);
            }
        }
        rge = RShape.createCircle(position.x, position.y, psize).toShape();
        a.noStroke();

        checkpColorChild(a);
        a.stroke(100);
        rge.draw(a);
    }

    public void checkpColor(PApplet t) {
        switch (getpState()) {
        case 0:
            t.stroke(74, 84, 94);
            break;
        case 1:
            t.stroke(0, 200, 200);
            break;
        case 2:
            t.stroke(255, 0, 0);
            break;
        case 3:
            t.stroke(255, 150, 0);
            break;
        }

    }

    public void checkpColorChild(PApplet t) {

        switch (getpState()) {
        case 0:
            psize = 5;// + mass/ 100;
            if ((this.getUV()[0] + this.getUV()[1]) % 2 == 0)
                t.fill(255);
            else
                t.fill(200);

            break;
        case 1:
            psize = 5;
            t.fill(0, 200, 200);
            break;
        case 2:
            psize = 5;
            t.fill(255, 0, 0);
            break;
        case 3:
            psize = 10;
            t.fill(255, 150, 0);
            break;
        }
    }

    public void drawMassDiagram() {
        p5.pushMatrix();
        p5.strokeWeight(0.3f);
        p5.stroke(100);
        p5.noFill();
        p5.translate(position.x, position.y, position.z);
        p5.ellipse(0, 0, mass / 10 + 5, mass / 10 + 5);
        p5.popMatrix();
        p5.strokeWeight(1);
    }

    public void attachTo(Particle P, float restingDist, float stiff) {
        Link lnk = new Link(p5, this, P, restingDist, stiff);
        links.add(lnk);
    }

    public void pinTo(Vec3D location) {
        this.setPinned(true);
        pinLocation.set(location);
    }

    public void pinToZ(float z) {
        this.setPinnedZ(true);
        pinLocation.z = z;
    }

    public void setMass(float mass) {
        this.mass += mass;
    }

    private void forceCalculate(float gravity) {
        switch (fState) {
        case 0:
            fg = new Vec3D(0, 0, mass * gravity);
            break;
        case 1:
            fg = position.sub(fc);
            fg.limit(mass * gravity);
            break;
        case 2:
            fg = position.sub(fc);
            fg.limit(mass * gravity);
            break;
        }
    }

    public void updatePhysics(float timeStep, float gravity) {

        forceCalculate(gravity);
        applyForce(fg);

        Vec3D velocity = position.sub(lastPosition);
        acceleration.subSelf(velocity.scale(damping / mass));
        Vec3D nextPos = acceleration.scale(0.5f).scale(timeStep * timeStep)
                .add(position.add(velocity));

        lastPosition.set(position);
        position.set(nextPos);
        acceleration.set(0, 0, 0);
    }

    public void applyForce(Vec3D f) {
        acceleration.addSelf(f.scaleSelf(1.0f / 1)); // scaleSelf(1.0f/mass);
    }

    public void solveConstraints() {
        for (Link l : links) {
            l.constraintSolve();
        }

        if (isPinned())
            position.set(pinLocation);
        if (isPinnedZ())
            position.z = 0;
    }

    public int[] getUV() {
        return uv;
    }

    public void setUV(int x, int y) {
        uv[0] = x;
        uv[1] = y;
    }

    public int getpState() {
        return pState;
    }

    public void setpState(int pState) {
        this.pState = pState;
    }

    public int getfState() {
        return fState;
    }

    public void setfState(int fState) {
        this.fState = fState;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void removeLink(Link lnk) {
        links.remove(lnk);
    }

    public Vec3D ptNormal(Particle a, Particle b, Particle c, Particle d) {
        Vec3D ac = a.position.sub(c.position);
        Vec3D bd = b.position.sub(d.position);
        Vec3D cros = bd.cross(ac);
        cros.normalize();
        return cros;
    }

    public float ptGetCurvature(Particle a, Particle b) {
        Vec3D u = a.position.sub(this.position);
        Vec3D v = b.position.sub(this.position);

        float deg = u.angleBetween(v, true);
        return deg;
    }

    public void updatePhysicsNormal(float timeStep, Vec3D gNormal) {
        Vec3D no = gNormal.copy();
        this.applyForce(no);
        Vec3D velocity = position.sub(lastPosition);
        acceleration.subSelf(velocity.scale(damping / mass));
        Vec3D nextPos = acceleration.scale(0.5f).scale(timeStep * timeStep)
                .add(position.add(velocity));
        lastPosition.set(position);
        position.set(nextPos);
        acceleration.set(0, 0, 0);
    }

    public void drawMeshPt() {
        if (p5.pause) {
            Vec3D mousePos = new Vec3D(position.x, position.y, position.z);
            p5.brush.drawAtAbsolutePos(mousePos, p5.density);
        }
    }

    public boolean isPinnedZ() {
        return pinnedZ;
    }

    public void setPinnedZ(boolean pinnedZ) {
        this.pinnedZ = pinnedZ;
    }

    public void together() {
        if (p5.together) {
            Vec3D u = new Vec3D();
            for (Particle t : p5.lockedp) {
                u.addSelf(t.position);
                u.scale(p5.lockedp.size());
            }
            for (Particle t : p5.lockedp)
                t.pinTo(u);
            p5.together = false;
        }
    }
}
