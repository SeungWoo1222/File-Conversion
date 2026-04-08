package com.toyboyz.fileconversion.worker.conversion.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import com.toyboyz.fileconversion.worker.message.dto.ParserDTO;
import com.toyboyz.fileconversion.infra.redis.service.RedisProgressPublisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionService {

    private final RedisProgressPublisher redisProgressPublisher;



    //word,docx -> pdf 변환
    //폰트가 꺠질 수 있는 문제 발생
    //서버가 들어오는 폰트를 가지고 있어야함
    //많이 사용되는 폰트를 파악하고 로컬에서 가지고 있어야 할 필요있음
    //별도의 폰트 매핑 함수가 필요함
    public byte[] docxToPdf(ParserDTO info,byte[] file) throws IOException {
        try (
                InputStream outStream = new ByteArrayInputStream(file);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Long historyId = info.getHistoryId();
            String uuid = info.getUuid();
            String filename = info.getFileName();

            redisProgressPublisher.publishProg(historyId,uuid,filename,0,null,"1",0);

            //docx 로드 (outStream 을 통해 직접 로드)
            WordprocessingMLPackage wordprocessingMLPackage = Docx4J.load(outStream);

            redisProgressPublisher.publishProg(historyId,uuid,filename,30,null,"2",0);

            //pdf로 변환하고 outStream에 기록
            Docx4J.toPDF(wordprocessingMLPackage, os);

            redisProgressPublisher.publishProg(historyId,uuid,filename,60,null,"2",0);

            //byte[] 리턴
                return os.toByteArray();
            } catch (Exception e) {
            throw new RuntimeException("Word -> PDF 변환 오류" ,e);
        }
    }




    //PNG, JPG (JPEG), GIF, BMP -> PDF 변환
    public byte[] imageToPdf(ParserDTO info,byte[] file) throws IOException {
        Long historyId = info.getHistoryId();
        String uuid = info.getUuid();
        String filename = info.getFileName();

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            redisProgressPublisher.publishProg(historyId,uuid,filename,0,null,"1",0);

            // PDF 생성 스트림,도큐멘트 초기화
            PdfWriter writer = new PdfWriter(outStream);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // 이미지를 iText 전용 ImageData 타입으로 래핑
            ImageData data = ImageDataFactory.create(file);
            Image image = new Image(data);

            // 이미지 사이즈 추출 -> pdf 사이즈로 설정
            pdfDoc.setDefaultPageSize(new PageSize(image.getImageWidth(), image.getImageHeight()));

            redisProgressPublisher.publishProg(historyId,uuid,filename,50,null,"2",0);

            // document 에 이미지 배치 (마진 좌표 0,0 기준 꽉 채움)
            Document document = new Document(pdfDoc);
            document.setMargins(0, 0, 0, 0);
            document.add(image);

            document.close();

            return outStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 변환 오류: " + e.getMessage());
        }
    }


    //변환 완료된 파일명을 반환하는 메서드
    //클라이언트가 가진 origin 파일명으로 다시 파싱해서 s3 에 올린다.
    //해당 url 을 master db history>convertedFile 의 필드에 넣어준다
    //그리고 sse 로 프론트에 쏴준다.
    public String convertedFilename(ParserDTO parserDTO) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("uploads/")
                    .append(parserDTO.getUuid()).append("/");
            //일단 true 일 경우 원본 파일명으로 준다
            sb.append(parserDTO.getOriginalFileName()).append(".").append(parserDTO.getRequestFormat());
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("완성 파일명 파싱 실패: " + e.getMessage());
        }
    }
}
