package com.kshrd.admsfileservice.employeemanage.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class SimplePdf {
    private SimplePdf() {
    }

    public static byte[] of(String title, List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 16 Tf\n72 740 Td\n(")
                .append(escape(title))
                .append(") Tj\n/F1 11 Tf\n14 TL\n0 -24 Td\n");
        for (String line : lines) {
            content.append("(").append(escape(line == null || line.isBlank() ? " " : line)).append(") '\n");
        }
        content.append("ET");
        byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int[] offsets = new int[6];
            out.write(new byte[] {'%', 'P', 'D', 'F', '-', '1', '.', '4', '\n',
                    '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});
            offsets[1] = out.size();
            out.write("1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[2] = out.size();
            out.write("2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[3] = out.size();
            out.write(("3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]"
                    + "/Contents 4 0 R/Resources<</Font<</F1 5 0 R>>>>>>endobj\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            offsets[4] = out.size();
            out.write(("4 0 obj<</Length " + stream.length + ">>stream\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write(stream);
            out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            offsets[5] = out.size();
            out.write("5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                    .getBytes(StandardCharsets.ISO_8859_1));
            int xrefAt = out.size();
            out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 5; i++) {
                out.write("%010d 00000 n \n".formatted(offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }
            out.write(("trailer<</Size 6/Root 1 0 R>>\nstartxref\n" + xrefAt + "\n%%EOF\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not build PDF", ex);
        }
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
