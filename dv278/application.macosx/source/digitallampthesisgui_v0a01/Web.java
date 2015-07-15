package digitallampthesisgui_v0a01;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec3D;

public class Web {

    DigitalLampThesisGUI_v0a01 p5;
    int webWidth;
    int webHeight;
    ArrayList<Particle> particles = new ArrayList<Particle>();

    private int fState = 0;

    float restingDistances = 20;
    float stiffnesses = 0.01f;
    int constraintAccuracy = 20;
    int fixedDeltaTime = 15;
    float fixedDeltaTimeSeconds = (float) fixedDeltaTime / 1000.0f;

    public Web() {
    }

    public Web(DigitalLampThesisGUI_v0a01 p5, int sW, int sH) {
        this.p5 = p5;
        webWidth = sW;
        webHeight = sH;

        for (int y = 0; y < webHeight; y++) {
            for (int x = 0; x < webWidth; x++) {
                Particle particle = new Particle(p5, new Vec3D(
                        (x - webWidth / 2) * restingDistances,
                        (y - webHeight / 2) * restingDistances, 0));
                // initial position
                particle.setUV(x, y);
                particles.add(particle);
            }
        }
        attachEven();
        pinCorner();
    }

    public void draw(int gravity, int normalValue) {
        for (Particle p : particles)
            p.draw();
        if (!p5.pause) {
            for (int x = 0; x < constraintAccuracy; x++)
                for (Particle p : particles)
                    p.solveConstraints();
            for (Particle p : particles)
                p.updatePhysics(fixedDeltaTimeSeconds, gravity);

            normalForceApply(normalValue);
        }
        if (!p5.pause)
            drawMesh();
    }

    public void drawApplet(PApplet a) {
        for (Particle p : particles)
            p.drawApplet(a);
    }

    private void pinCorner() {
        for (Particle p : particles) {
            if ((p.getUV()[0] == 0 && (p.getUV()[1] == 0 || (p.getUV()[1] == webHeight - 1)))
                    || (p.getUV()[0] == webWidth - 1 && (p.getUV()[1] == 0 || (p
                            .getUV()[1] == webHeight - 1)))) {
                p.pinTo(p.position.scale(0.7f));
                p.setpState(2);
            }
        }
    }

    private void attachEven() {
        for (Particle p : particles) {
            if ((p.getUV()[0] + p.getUV()[1]) % 2 == 0) {

                if (p.getUV()[1] != 0) // / attach to up
                    p.attachTo(
                            particles.get((p.getUV()[1] - 1) * (webWidth)
                                    + p.getUV()[0]), restingDistances,
                            stiffnesses);
                if (p.getUV()[0] != 0) // / attach to left
                    p.attachTo(
                            particles.get((p.getUV()[1]) * (webWidth)
                                    + p.getUV()[0] - 1), restingDistances,
                            stiffnesses);
                if (p.getUV()[1] != webHeight - 1) // / attach to down
                    p.attachTo(
                            particles.get((p.getUV()[1] + 1) * (webWidth)
                                    + p.getUV()[0]), restingDistances,
                            stiffnesses);
                if (p.getUV()[0] != webWidth - 1) // / attach to right
                    p.attachTo(
                            particles.get((p.getUV()[1]) * (webWidth)
                                    + p.getUV()[0] + 1), restingDistances,
                            stiffnesses);
            }
        }
    }

    public void reConstruct(int sW, int sH) {
        webWidth = sW;
        webHeight = sH;

        for (int y = 0; y < webHeight; y++) {
            for (int x = 0; x < webWidth; x++) {
                Particle particle = new Particle(p5, new Vec3D(
                        (x - webWidth / 2) * restingDistances,
                        (y - webHeight / 2) * restingDistances, 0));
                particle.setUV(x, y);
                particles.add(particle);
            }
        }
        attachEven();
        pinCorner();
    }

    private void drawMesh() {
        for (Particle p : particles) {
            if (p.getUV()[1] != 0 && p.getUV()[0] != 0) {
                Particle p0 = particles.get((p.getUV()[1]) * (webWidth)
                        + p.getUV()[0]);
                Particle p1 = particles.get((p.getUV()[1]) * (webWidth)
                        + p.getUV()[0] - 1);
                Particle p2 = particles.get((p.getUV()[1] - 1) * (webWidth)
                        + p.getUV()[0] - 1);
                Particle p3 = particles.get((p.getUV()[1] - 1) * (webWidth)
                        + p.getUV()[0]);
                p5.noStroke();
                p5.fill(191, 202, 212, 100);
                p5.mesh.addFace(p0.position, p1.position, p2.position);
                p5.mesh.addFace(p0.position, p2.position, p3.position);
            }
        }
    }

    public void normalForceApply(float normalValue) {
        if (p5.toggle) {

            for (Particle p : particles) {

                int[] t = p.getUV();
                if ((t[0] > 0 && t[1] > 0 && t[0] < webWidth - 1 && t[1] < webHeight - 1)
                        && p.links.size() > 3) {

                    Particle a = particles
                            .get((webWidth) * (t[1] - 1) + (t[0]));
                    Particle b = particles
                            .get((webWidth) * (t[1]) + (t[0] - 1));
                    Particle c = particles
                            .get((webWidth) * (t[1] + 1) + (t[0]));
                    Particle d = particles
                            .get((webWidth) * (t[1]) + (t[0] + 1));

                    float deg1 = p.ptGetCurvature(a, c);
                    float deg2 = p.ptGetCurvature(b, d);
                    if (deg1 > PConstants.PI / 3 && deg2 > PConstants.PI / 3) {

                        Vec3D n = p.ptNormal(a, b, c, d);
                        n.normalizeTo(normalValue);
                        n.scaleSelf(p.position.z);
                        Vec3D show = n.copy();
                        show.scaleSelf(1.0f / 30);
                        Vec3D ad = p.position.add(show);

                        p5.stroke(n.magnitude() / 6, 0, n.magnitude() / 12,
                                n.magnitude() / 6 - 100);
                        p5.line(p.position.x, p.position.y, p.position.z, ad.x,
                                ad.y, ad.z);

                        p.updatePhysicsNormal(fixedDeltaTimeSeconds, n);
                    }
                }
            }
        }
    }

    public Particle getUV(int u, int v) {
        Particle np = new Particle(p5);
        for (Particle p : particles) {
            if (p.getUV()[0] == u && p.getUV()[1] == v) {
                np = p;
                break;
            }
        }
        return np;
    }

    public int getfState() {
        return fState;
    }

    public void setfState(int fState) {
        for (Particle p : particles)
            p.setfState(fState);
    }
}
