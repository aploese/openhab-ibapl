/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2025, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.ibapl.openhab.automation.solarmanager.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarManager} provides the necessary methods for retrieving part(s) of the Solar Manager calculations
 * and it provides the method for the overall Solar Manager calculations. It also resets the Solar Manager controller
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
class SolarManager {
    private double integralResult;
    private double derivativeResult;
    private double previousError;

    private double kp;
    private double ki;
    private double kd;
    private double derivativeTimeConstantSec;
    private double iMinResult;
    private double iMaxResult;

    public SolarManager(double kpAdjuster, double kiAdjuster, double kdAdjuster, double derivativeTimeConstantSec,
            double iMinValue, double iMaxValue, double previousIntegralPart, double previousDerivativePart,
            double previousError) {
        this.kp = kpAdjuster;
        this.ki = kiAdjuster;
        this.kd = kdAdjuster;
        this.derivativeTimeConstantSec = derivativeTimeConstantSec;
        this.iMinResult = Double.NaN;
        this.iMaxResult = Double.NaN;

        // prepare min/max, restore previous state for the integral result accumulator
        if (Double.isFinite(kiAdjuster) && Math.abs(kiAdjuster) > 0.0) {
            if (Double.isFinite(iMinValue)) {
                this.iMinResult = iMinValue / kiAdjuster;
            }
            if (Double.isFinite(iMaxValue)) {
                this.iMaxResult = iMaxValue / kiAdjuster;
            }
            if (Double.isFinite(previousIntegralPart)) {
                this.integralResult = previousIntegralPart / kiAdjuster;
            }
        }

        // restore previous state for the derivative result accumulator
        if (Double.isFinite(kdAdjuster) && Math.abs(kdAdjuster) > 0.0) {
            if (Double.isFinite(previousDerivativePart)) {
                this.derivativeResult = previousDerivativePart / kdAdjuster;
            }
        }

        // restore previous state for the previous error variable
        if (Double.isFinite(previousError)) {
            this.previousError = previousError;
        }
    }

    public SolarManmagetrOutputDTO calculate(double mainPower, long lastInvocationMs, int loopTimeMs) {
        final double lastInvocationSec = lastInvocationMs / 1000d;
        final double error = mainPower;

        // derivative T1 calculation
        final double timeQuotient = lastInvocationSec / derivativeTimeConstantSec;
        if (derivativeTimeConstantSec != 0) {
            previousError = error;
        }

        // integral calculation
        integralResult += error * lastInvocationMs / loopTimeMs;
        if (Double.isFinite(iMinResult)) {
            integralResult = Math.max(integralResult, iMinResult);
        }
        if (Double.isFinite(iMaxResult)) {
            integralResult = Math.min(integralResult, iMaxResult);
        }

        // calculate parts
        final double proportionalPart = kp * error;

        double integralPart = ki * integralResult;

        final double derivativePart = kd * derivativeResult;

        final double output = proportionalPart + integralPart + derivativePart;

        return new SolarManmagetrOutputDTO(output, proportionalPart, integralPart, derivativePart, error);
    }

    public void setIntegralResult(double integralResult) {
        this.integralResult = integralResult;
    }

    public void setDerivativeResult(double derivativeResult) {
        this.derivativeResult = derivativeResult;
    }
}
