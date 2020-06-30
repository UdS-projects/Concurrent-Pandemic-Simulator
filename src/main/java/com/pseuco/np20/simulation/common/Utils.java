package com.pseuco.np20.simulation.common;

import com.pseuco.np20.model.Rectangle;
import com.pseuco.np20.model.Scenario;
import com.pseuco.np20.model.XY;

/**
 * Some utility functions you may find useful.
 */
public class Utils {
    /**
     * Computes whether it is possible to propagate information from a <em>source area</em>
     * to a <em>target area</em>.
     *
     * You may use this method to check whether it is possible to propagate information from
     * the padding of a patch inside the area owned by the patch. If you do not want to use
     * this method make sure your method is as least as precise as this method.
     *
     * For those who would like to earn a bonus: In some cases this method returns that
     * information may propagate although on closer inspection this is not the case. What
     * are those cases? Can you improve on that?
     *
     * @param scenario The scenario to check for obstacles and use the parameters from.
     * @param source The <em>source area</em> for which to check the propagation possibility.
     * @param target The <em>target area</em> for which to check the propagation possibility.
     * @return Whether information may propagate from the <em>source</em> to the <em>target area</em>.
     */
    static public boolean mayPropagateFrom(
        final Scenario scenario,
        final Rectangle source,
        final Rectangle target
    ) {
        for (final XY sourceCell : source) {
            if (scenario.onObstacle(sourceCell)) {
                continue;
            }
            for (final XY targetCell : target) {
                if (scenario.onObstacle(targetCell)) {
                    continue;
                }
                final int deltaX = Math.abs(sourceCell.getX() - targetCell.getX());
                final int deltaY = Math.abs(sourceCell.getY() - targetCell.getY());
                final int distance = deltaX + deltaY;
                if (deltaX <= 1 && deltaY <= 1) {
                    return true;
                }
                if (distance <= scenario.getParameters().getInfectionRadius()) {
                    return true;
                }
            }
        }

        return false;
    }
}