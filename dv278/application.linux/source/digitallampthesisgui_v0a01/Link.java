package digitallampthesisgui_v0a01;

import processing.core.PApplet;
import toxi.geom.Line3D;
import toxi.geom.Vec3D;

public class Link {

    DigitalLampThesisGUI_v0a01 p5;

    float stiff;
    float restingDist;
    Particle p1;
    Particle p2;

    float scalarP1;
    float scalarP2;
    Line3D segment;

    public Link(DigitalLampThesisGUI_v0a01 p5, Particle p1, Particle p2,
            float restingDist, float stiff) {
        this.p5 = p5;
        this.p1 = p1;
        this.p2 = p2;
        this.restingDist = restingDist;
        this.stiff = stiff;

        float im1 = 1 / this.p1.mass;
        float im2 = 1 / this.p2.mass;
        scalarP1 = (im1 / (im1 + im2)) * stiff;
        scalarP2 = (im2 / (im1 + im2)) * stiff;
    }

    public void draw() {

        segment = new Line3D(p1.position, p2.position);
        p5.gfx.line(segment);

        if (p5.pause && (p1.getUV()[0] % 2 == 0 || p1.getUV()[1] % 2 == 0)) {

            p5.builder.createLattice(p5.brush, segment, 0.9f);
        }
    }

    public void drawApplet(PApplet a) {
        a.stroke(235, 57, 174, 200);
        a.line(p1.position.x, p1.position.y, p2.position.x, p2.position.y);
    }

    public void constraintSolve() {
        Vec3D delta = p1.position.sub(p2.position);
        float d = p1.position.distanceTo(p2.position);
        if (d != 0) {
            float difference = (restingDist - d) / d;

            p1.position.addSelf(delta.scale(scalarP1 * difference));
            p2.position.subSelf(delta.scale(scalarP2 * difference));
        }
    }
}
