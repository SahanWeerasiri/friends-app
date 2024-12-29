package com.example.friends;

public class MyPdfCreator {
    public MyPdfCreator(){

    }
    public void createPdf(){

       /* try{String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            File f=new File(path,"Library_NON_Translated.pdf");

            PdfWriter pdfWriter=new PdfWriter(f);
            PdfDocument pdfDocument=new PdfDocument(pdfWriter);
            Document doc=new Document(pdfDocument);

            doc.add(new Paragraph(new Text(headings[1]+"\n\n").setBold().setUnderline()));
            for(DataSnapshot ds:snapshot.getChildren()) {
                ViewData data = ds.getValue(ViewData.class);
                byte[] im = Base64.decode(data.getImage(), Base64.DEFAULT);
                ImageData d = ImageDataFactory.create(im);
                Image img = new Image(d).setWidth(200f).setHeight(200f);
                doc.add(img);
                doc.add(new Paragraph("Name\t:\t" + data.getName()));

                doc.add(new Paragraph("Author\t:\t" + data.getAuthor()));


                doc.add(new Paragraph("Type\t:\t" + data.getType()));
                doc.add(new Paragraph("Date\t:\t" + data.getDate()));
                doc.add(new Paragraph("Time\t:\t" + data.getTime()));
                doc.add(new Paragraph("Description\t:\t" + data.getDescription()));
                doc.add(new Paragraph("______________\n\n"));


            }
            doc.close();


        }catch (Exception e){

        }
*/
    }
}
