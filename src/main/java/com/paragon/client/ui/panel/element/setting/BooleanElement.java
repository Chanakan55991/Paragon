package com.paragon.client.ui.panel.element.setting;

import com.paragon.api.util.render.RenderUtil;
import com.paragon.api.setting.Bind;
import com.paragon.api.setting.Setting;
import com.paragon.client.ui.animation.Animation;
import com.paragon.client.ui.animation.Easing;
import com.paragon.client.ui.panel.Click;
import com.paragon.client.ui.panel.element.Element;
import com.paragon.client.ui.panel.element.module.ModuleElement;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

@SuppressWarnings("unchecked")
public final class BooleanElement extends Element {

    private final Setting<Boolean> setting;

    private final Animation enabledAnimation = new Animation(() -> 200f, false, () -> Easing.LINEAR);

    public BooleanElement(int layer, Setting<Boolean> setting, ModuleElement moduleElement, float x, float y, float width, float height) {
        super(layer, x, y, width, height);

        setParent(moduleElement.getParent());
        this.setting = setting;

        setting.getSubsettings().forEach(subsetting -> {
            if (subsetting.getValue() instanceof Boolean) {
                getSubElements().add(new BooleanElement(layer + 1, (Setting<Boolean>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Enum<?>) {
                getSubElements().add(new EnumElement(layer + 1, (Setting<Enum<?>>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Number) {
                getSubElements().add(new SliderElement(layer + 1, (Setting<Number>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Bind) {
                getSubElements().add(new BindElement(layer + 1, (Setting<Bind>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof Color) {
                getSubElements().add(new ColourElement(layer + 1, (Setting<Color>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            } else if (subsetting.getValue() instanceof String) {
                getSubElements().add(new StringElement(layer + 1, (Setting<String>) subsetting, moduleElement, getX(), getY(), getWidth(), getHeight()));
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, int dWheel) {
        if (setting.isVisible()) {
            enabledAnimation.setState(setting.getValue());

            RenderUtil.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(40, 40, 45).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), getWidth() - getLayer() * 2, getHeight(), new Color((int) (40 + (30 * getHover().getAnimationFactor())), (int) (40 + (30 * getHover().getAnimationFactor())), (int) (45 + (30 * getHover().getAnimationFactor()))).getRGB());
            RenderUtil.drawRect(getX() + getLayer(), getY(), 1, (float) (getHeight() * enabledAnimation.getAnimationFactor()), Color.HSBtoRGB(getParent().getLeftHue() / 360, 1f, (float) (0.5f + (0.25f * getHover().getAnimationFactor()))));

            renderText(setting.getName(), getX() + (getLayer() * 2) + 5, getY() + getHeight() / 2 - 3.5f, 0xFFFFFFFF);

            if (!getSubElements().isEmpty()) {
                renderText("...", getX() + getWidth() - getStringWidth("...") - 5, getY() + 2f, -1);
            }

            super.render(mouseX, mouseY, dWheel);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && getParent().isElementVisible(this) && click.equals(Click.LEFT)) {
                setting.setValue(!setting.getValue());
            }

            super.mouseClicked(mouseX, mouseY, click);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, Click click) {
        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click);
        }
    }

    @Override
    public void keyTyped(int keyCode, char keyChar) {
        if (setting.isVisible()) {
            super.keyTyped(keyCode, keyChar);
        }
    }

    @Override
    public float getHeight() {
        return getSetting().isVisible() ? super.getHeight() : 0;
    }

    @Override
    public float getSubElementsHeight() {
        return getSetting().isVisible() ? super.getSubElementsHeight() : 0;
    }

    @Override
    public float getTotalHeight() {
        return getSetting().isVisible() ? super.getTotalHeight() : 0;
    }

    public Setting<Boolean> getSetting() {
        return setting;
    }

}
