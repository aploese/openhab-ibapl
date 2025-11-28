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
package de.ibapl.openhab.automation.solarmanager.internal.template;

import de.ibapl.openhab.automation.solarmanager.internal.handler.SolarManagerTriggerHandler;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;

/**
 *
 * @author Arne Plöse - Initial Contribution
 */
@NonNullByDefault
public class SolarManagerRuleTemplate extends RuleTemplate {
    public static final String UID = "SolarManangerRuleTemplate";

    public static SolarManagerRuleTemplate initialize() {
        final String triggerId = UUID.randomUUID().toString();

        final List<Trigger> triggers = List.of(ModuleBuilder.createTrigger().withId(triggerId)
                .withTypeUID(SolarManagerTriggerHandler.MODULE_TYPE_ID).withLabel("Solar Manager Trigger").build());

        return new SolarManagerRuleTemplate(Set.of("Solar Manager Controller"), triggers, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
    }

    public SolarManagerRuleTemplate(Set<String> tags, List<Trigger> triggers, List<Condition> conditions,
            List<Action> actions, List<ConfigDescriptionParameter> configDescriptions) {
        super(UID, "Solar Manager Controller", "Template for a Solar Manager controlled rule", tags, triggers, conditions, actions,
                configDescriptions, Visibility.VISIBLE);
    }
}
