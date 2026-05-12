const fs = require('fs');
const path = require('path');

const sizeMap = { '10': 'test_10mb.png', '15': 'test_15mb.png', '19': 'test_19mb.png' };
const imageFile = sizeMap[process.env.IMAGE] || 'test_10mb.png';
const imagePath = path.join(__dirname, imageFile);
let imageBuffer;

try {
    imageBuffer = fs.readFileSync(imagePath);
    console.log(`✅ 테스트 이미지 로드 완료: ${imageFile} (${(imageBuffer.length / 1024 / 1024).toFixed(2)} MB)`);
} catch (e) {
    console.error(`🚨 테스트 이미지(${imageFile})를 찾을 수 없습니다!`, e);
    process.exit(1);
}

module.exports = {
    generateRandomData: function (requestParams, context, ee, next) {
        context.vars = context.vars || {};

        const requestId = Math.floor(Math.random() * 1000000);

        context.vars.filename = `test_file_${requestId}.jpg`;
        context.vars.imageSize = imageBuffer.length;
        return next();
    },

    setImageBody: function (requestParams, context, ee, next) {
        requestParams.body = imageBuffer;
        return next();
    }
};