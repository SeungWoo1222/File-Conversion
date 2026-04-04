//package com.toyboyz.fileconversion.api.history.entity;
//
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class HistoryTest {
//
//
//
//    @Test
//    @DisplayName("기록이 생성되는지 확인하는 테스트")
//    void saveHistory() {
//
//        History history = History.builder()
//                .historyId(1L)
//                .uuid("jw")
//                .fileName("file")
//                .originalFile("이력서")
//                .originalFormat("PDF")
//                .status("1")
//                .build();
//
//        Assertions.assertThat(history.getHistoryId()).isEqualTo(1L);
//        Assertions.assertThat(history.getUuid()).isEqualTo("jw");
//        Assertions.assertThat(history.getFileName()).isEqualTo("file");
//        Assertions.assertThat(history.getOriginalFile()).isEqualTo("이력서");
//        Assertions.assertThat(history.getOriginalFormat()).isEqualTo("PDF");
//        Assertions.assertThat(history.getStatus()).isEqualTo("1");
//    }
//
//
//
//}