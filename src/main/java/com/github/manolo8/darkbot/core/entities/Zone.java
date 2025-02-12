package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.pathfinder.PolygonImpl;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Zone extends Entity implements eu.darkbot.api.game.entities.Zone {

    private final ObjArray pointsArr = ObjArray.ofVector(true);
    private final List<Position> points = new ArrayList<>();
    private final PolygonImpl zoneArea = new PolygonImpl(points);
    private boolean useBounds;

    Zone(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        super.update();

        pointsArr.update(API.readMemoryLong(address + 216));
        if (pointsArr.getSize() < 3) return;

        if (pointsArr.syncAndReport(points, Position::new)) {
            zoneArea.invalidateBounds();
            useBounds = isRectangle(zoneArea);
        }
    }

    private static boolean isRectangle(Area.Polygon polygon) {
        if (polygon.getVertices().size() != 4) return false;
        Area.Rectangle bounds = polygon.getBounds();

        return polygon.getVertices().stream()
                .allMatch(v -> (v.getX() == bounds.getX() || v.getX() == bounds.getX2()) &&
                        (v.getY() == bounds.getY() || v.getY() == bounds.getY2()));
    }

    @Override
    public Area getZoneArea() {
        return useBounds ? zoneArea.getBounds() : zoneArea;
    }

    private static class Position extends Updatable.Reporting implements Locatable {

        private double x, y;

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;
            double newX = API.readMemoryDouble(address + 32),
                    newY = API.readMemoryDouble(address + 40);

            if (this.x == newX && this.y == newY) return false;
            this.x = newX;
            this.y = newY;
            return true;
        }
    }
}
