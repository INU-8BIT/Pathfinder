package com.inu8bit.pathfinder;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class Protocol implements Serializable {

    // 프로토콜 타입에 관한 변수
    public static final int PT_UNDEFINED 		= -1; 	// 프로토콜이 지정되어 있지 않을 경우에
    public static final int PT_EXIT 			= 0;	// 종료
    public static final int PT_REQ_LOGIN 		= 1;	// 로그인요청
    public static final int PT_RES_LOGIN 		= 2; 	// 인증요청
    public static final int PT_LOGIN_RESULT 	= 3; 	// 인증결과
    public static final int LEN_ID 				= 20; 	// ID길이
    public static final int LEN_PASSWORD 		= 20; 	// PW길이
    public static final int LEN_RESULT 			= 2; 	// 로그인인증값 길이
    public static final int LEN_PROTOCOL_TYPE 	= 1; 	// 프로토콜타입 길이
    public static final int LEN_MAX 			= 1000; // 최대 데이타 길이
    public static final int PT_REQ_REGISTER	 	= 4; 	// 회원가입 요청
    public static final int PT_RES_REGISTER 	= 5; 	// 회원가입 요청
    public static final int PT_REGISTER_RESULT 	= 6; 	// 회원가입 결과
    public static final int PT_REQ_NFC 			= 7; 	// NFC 등록 요청
    public static final int PT_RES_NFC 			= 8; 	// NFC 요청
    public static final int PT_NFC_RESULT		= 9; 	// NFC 결과
    public static final int LEN_NFC_ID 			= 20; 	// NFC 값 길이
    public static final int LEN_NFC_NAME		= 50; 	// NFC 이름
    public static final int LEN_NFC_INFO		= 200; 	// NFC 정보

    protected int protocolType;

    private byte[] packet; // 프로토콜과 데이터의 저장공간이 되는 바이트배열

    // 생성자
    public Protocol() {
        this(PT_UNDEFINED);
    }

    // 생성자
    public Protocol(int protocolType) {

        this.protocolType = protocolType;

        // 어떤 상수를 생성자에 넣어 Protocol 클래스를 생성하느냐에 따라서 바이트배열 packet 의 length 가 결정된다.
        getPacket(protocolType);
    }

    public byte[] getPacket(int protocolType) {

        if (packet == null) {

            switch (protocolType) {

                case PT_REQ_LOGIN:
                    packet = new byte[LEN_PROTOCOL_TYPE];
                    break;
                case PT_RES_LOGIN:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_ID + LEN_PASSWORD];
                    break;
                case PT_UNDEFINED:
                    packet = new byte[LEN_MAX];
                    break;
                case PT_LOGIN_RESULT:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_RESULT];
                    break;
                case PT_RES_REGISTER:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_ID + LEN_PASSWORD];
                    break;
                case PT_REGISTER_RESULT:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_RESULT];
                    break;
                case PT_REQ_NFC:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO];
                    break;
                case PT_RES_NFC:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO];
                    break;
                case PT_NFC_RESULT:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO + LEN_RESULT];
                    break;
                case PT_EXIT:
                    packet = new byte[LEN_PROTOCOL_TYPE];
                    break;
            }
        }

        packet[0] = (byte) protocolType; // packet 바이트배열의 첫번째 방에 프로토콜타입 상수를 셋팅해 놓는다.
        return packet;
    }

    // 로그인후 성공/실패의 결과값을 프로토콜로 부터 추출하여 문자열로 리턴
    public String getLoginResult() {
        // String의 다음 생성자를 사용 : String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_RESULT).trim();
    }

    // String ok를 byte[] 로 만들어서 packet의 프로토콜 타입 바로 뒤에 추가한다.
    public void setLoginResult(String ok) {
        // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, ok.trim().getBytes().length);
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public byte[] getPacket() {
        return packet;
    }

    // Default 생성자로 생성한 후 Protocol 클래스의 packet 데이타를 바꾸기 위한 메서드
    public void setPacket(int pt, byte[] buf) {
        packet = null;
        packet = getPacket(pt);
        protocolType = pt;
        System.arraycopy(buf, 0, packet, 0, packet.length);
    }

    public String getId() {
        // String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_ID).trim();
    }

    // byte[] packet 에 String NFCID를 byte[]로 만들어 프로토콜 타입 바로 뒷부분에 추가한다.
    public void setId(String id) {
        System.arraycopy(id.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, id.trim().getBytes().length);
    }

    public String getPassword() {
        // 구성으로 보아 패스워드는 byte[] 에서 로그인 아이디 바로 뒷부분에 들어가는 듯 하다.
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_ID, LEN_PASSWORD).trim();
    }

    public void setPassword(String password) {
        System.arraycopy(password.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_ID,
                password.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE + LEN_ID + password.trim().getBytes().length] = '\0';
    }


    // String ok를 byte[] 로 만들어서 packet의 프로토콜 타입 바로 뒤에 추가한다.
    public void setRegisterResult(String ok) {
        // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, ok.trim().getBytes().length);
    }

    // 회원가입 후 성공/실패의 결과값을 프로토콜로 부터 추출하여 문자열로 리턴
    public String getRegisterResult() {
        // String의 다음 생성자를 사용 : String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_RESULT).trim();
    }

    // byte[] packet 에 String ID를 byte[]로 만들어 프로토콜 타입 바로 뒷부분에 추가한다.
    public void setNFCID(String nfcID) {
        System.arraycopy(nfcID.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_ID, nfcID.trim().getBytes().length);
    }

    public String getNFCID() {
        // String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_ID, LEN_NFC_ID).trim();
    }

    public String getNFCName() throws UnsupportedEncodingException {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID, LEN_NFC_NAME, "UTF-8").trim();
    }

    public void setNFCName(String nfc_name) throws UnsupportedEncodingException {
        System.arraycopy(nfc_name.trim().getBytes("UTF-8"), 0, packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID,
                nfc_name.trim().getBytes("UTF-8").length);
    }

    public String getNFCInfo() throws UnsupportedEncodingException {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME, LEN_NFC_INFO, "UTF-8").trim();
    }

    public void setNFCInfo(String nfc_info) throws UnsupportedEncodingException {
        System.arraycopy(nfc_info.trim().getBytes("UTF-8"), 0, packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME,
                nfc_info.trim().getBytes("UTF-8").length);
    }

    public void setNFCResult(String ok) throws UnsupportedEncodingException {
        // arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO, ok.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO + ok.trim().getBytes().length] = '\0';
    }
    public String getNFCResult() {
        // String의 다음 생성자를 사용 : String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_ID + LEN_NFC_ID + LEN_NFC_NAME + LEN_NFC_INFO, LEN_RESULT).trim();
    }
}