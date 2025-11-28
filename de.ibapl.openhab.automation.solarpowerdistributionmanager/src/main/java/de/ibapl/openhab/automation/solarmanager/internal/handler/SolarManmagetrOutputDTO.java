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

/**
 *
 * @author Arne Plöse - Initial Contribution
 */
public class SolarManmagetrOutputDTO {
    private final double devece_1_outputPowerSetpoint;
    private final double proportionalPart;
    private final double integralPart;
    private final double derivativePart;
    private final double error;

    public SolarManmagetrOutputDTO(double output, double proportionalPart, double integralPart, double derivativePart,
            double error) {
        this.devece_1_outputPowerSetpoint = output;
        this.proportionalPart = proportionalPart;
        this.integralPart = integralPart;
        this.derivativePart = derivativePart;
        this.error = error;
    }

    public double getDevece_1_outputPowerSetpoint() {
        return devece_1_outputPowerSetpoint;
    }

    public double getProportionalPart() {
        return proportionalPart;
    }

    public double getIntegralPart() {
        return integralPart;
    }

    public double getDerivativePart() {
        return derivativePart;
    }

    public double getError() {
        return error;
    }
}
