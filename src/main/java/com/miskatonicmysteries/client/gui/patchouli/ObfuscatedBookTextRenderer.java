package com.miskatonicmysteries.client.gui.patchouli;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.client.book.text.Word;
import vazkii.patchouli.common.book.Book;

import java.util.List;

public class ObfuscatedBookTextRenderer { //did I just copy it? yes
    private final Book book;
    private final GuiBook gui;
    private final String text;
    private final int x, y, width;
    private final int lineHeight;
    private final Style baseStyle;

    private List<Word> words;

    public ObfuscatedBookTextRenderer(GuiBook gui, String text, int x, int y) {
        this(gui, text, x, y, GuiBook.PAGE_WIDTH, GuiBook.TEXT_LINE_HEIGHT, gui.book.textColor);
    }

    public ObfuscatedBookTextRenderer(GuiBook gui, String text, int x, int y, int width, int lineHeight, int baseColor) {
        this.book = gui.book;
        this.gui = gui;
        if (book.i18n) {
            this.text = I18n.translate(text);
        } else {
            this.text = text;
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.lineHeight = lineHeight;
        this.baseStyle = book.getFontStyle().withColor(TextColor.fromRgb(baseColor));
        build();
    }

    private void build() {
        BookTextParser parser = new BookTextParser(gui, book, x, y, width, lineHeight, baseStyle);
        words = parser.parse(text);
    }

    public void render(MatrixStack ms, int mouseX, int mouseY) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        Style style = book.getFontStyle();
        words.forEach(word -> word.render(ms, font, style, mouseX, mouseY));
    }

    public boolean click(double mouseX, double mouseY, int mouseButton) {
        for (Word word : words) {
            if (word.click(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }

        return false;
    }
}
