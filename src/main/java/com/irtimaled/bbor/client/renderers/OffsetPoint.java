package com.irtimaled.bbor.client.renderers;

import com.irtimaled.bbor.client.models.Point;
import com.irtimaled.bbor.common.models.Coords;

class OffsetPoint {
    private final Point point;

    OffsetPoint(double x, double y, double z) {
        this(new Point(x, y, z));
    }

    OffsetPoint(Coords coords) {
        this(new Point(coords));
    }

    OffsetPoint(Point point) {
        this.point = point;
    }

    double getX() {
        return point.getX();
    }

    double getY() {
        return point.getY();
    }

    double getZ() {
        return point.getZ();
    }

    OffsetPoint offset(double x, double y, double z) {
        return new OffsetPoint(point.offset(x, y, z));
    }

    double getDistance(OffsetPoint offsetPoint) {
        return this.point.getDistance(offsetPoint.point);
    }
}
