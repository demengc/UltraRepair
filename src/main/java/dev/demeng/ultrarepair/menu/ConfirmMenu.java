/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.ultrarepair.menu;

import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.serialize.ItemSerializer;
import dev.demeng.pluginbase.text.Text;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.bukkit.configuration.ConfigurationSection;

public class ConfirmMenu extends Menu {

  public ConfirmMenu(
      ConfigurationSection section, long cooldown, double cost,
      boolean bypassingCooldown, boolean bypassingCost, Runnable confirmAction) {
    super(section.getInt("size"), Objects.requireNonNull(section.getString("title")));

    final String cooldownPlaceholder;
    final String costPlaceholder;

    if (bypassingCooldown) {
      cooldownPlaceholder = section.getString("bypassed-cooldown-placeholder",
          "0 seconds &e(Cooldown Bypassed)");
    } else {
      cooldownPlaceholder = Time.formatDuration(DurationFormatter.LONG, cooldown);
    }

    if (bypassingCost) {
      costPlaceholder = section.getString("bypassed-cost-placeholder",
          "0 &e(Cost Bypassed)");
    } else {
      costPlaceholder = String.format("%.2f", cost);
    }

    final UnaryOperator<String> placeholders = str -> Text.colorize(str)
        .replace("%cooldown%", cooldownPlaceholder)
        .replace("%cost%", costPlaceholder);

    final ConfigurationSection confirmSection = section.getConfigurationSection("confirm");
    Objects.requireNonNull(confirmSection, "Confirm menu confirm section is null");
    final ConfigurationSection cancelSection = section.getConfigurationSection("cancel");
    Objects.requireNonNull(cancelSection, "Confirm menu cancel section is null");

    for (int slot : confirmSection.getIntegerList("slots")) {
      addButton(slot - 1, ItemSerializer.deserialize(confirmSection, placeholders),
          e -> confirmAction.run());
    }

    for (int slot : cancelSection.getIntegerList("slots")) {
      addButton(slot - 1, ItemSerializer.deserialize(cancelSection, placeholders),
          e -> e.getWhoClicked().closeInventory());
    }

    final ConfigurationSection fillersSection = section.getConfigurationSection("fillers");
    if (fillersSection != null) {
      applyFillersFromConfig(fillersSection);
    }
  }
}
