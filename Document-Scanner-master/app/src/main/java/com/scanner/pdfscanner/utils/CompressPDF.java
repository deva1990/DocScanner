package com.scanner.pdfscanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressPDF {

/** The resulting PDF file. */
//public static String RESULT = "results/part4/chapter16/resized_image.pdf";
/** The multiplication factor for the image. */
public static float FACTOR = 0.5f;

/**
 * compressPDF a PDF file src with the file dest as result
 * @param src the original PDF
 * @param dest the resulting PDF
 * @throws IOException
 * @throws DocumentException 
 */
public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
    // Read the file
    PdfReader reader = new PdfReader(src);
    int n = reader.getXrefSize();
    PdfObject object;
    PRStream stream;
    // Look for image and manipulate image stream
    for (int i = 0; i < n; i++) {
        object = reader.getPdfObject(i);
        if (object == null || !object.isStream())
            continue;
        stream = (PRStream)object;
        // if (value.equals(stream.get(key))) {
        PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
        System.out.println(stream.type());
        if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
            PdfImageObject image = new PdfImageObject(stream);
            byte[] imageBytes= image.getImageAsBytes();

            Bitmap bmp;
            bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bmp == null) continue;

            int width=bmp.getWidth();
            int height=bmp.getHeight();

            Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas outCanvas=new Canvas(outBitmap);
            outCanvas.drawBitmap(bmp, 0f, 0f, null);

            ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();

            outBitmap.compress(Bitmap.CompressFormat.JPEG, 10, imgBytes);

            stream.setData(imgBytes.toByteArray(), false, PdfStream.BEST_COMPRESSION);
            stream.put(PdfName.FILTER, PdfName.DCTDECODE);
            //stream.put(PdfName.FILTER, PdfName.FLATEDECODE);
            imgBytes.close();
        }

        /*object = reader.getPdfObject(i);
        if (object == null || !object.isStream())
            continue;
        stream = (PRStream)object;
        // if (value.equals(stream.get(key))) {
        PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
        System.out.println(stream.type());
        if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
            byte[] streamBytes= PdfReader.getStreamBytes(stream);
            stream.setData(streamBytes, true, PdfStream.BEST_COMPRESSION);
        }*/
    }
    // Save altered PDF
    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
    stamper.close();
    reader.close();
}

/**
 * Main method.
 *
 * @param    args    no arguments needed
 * @throws DocumentException 
 * @throws IOException
 */
public static void main(String[] args) throws IOException, DocumentException {
    //createPdf(RESULT);
    new CompressPDF().manipulatePdf("F:\\Deva\\pdf\\input.pdf", "F:\\Deva\\pdf\\output.pdf");
}

}