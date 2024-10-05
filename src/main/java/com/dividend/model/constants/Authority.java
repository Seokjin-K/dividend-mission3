package com.dividend.model.constants;

public enum Authority {
    ROLE_READ,
    ROLE_WRITE
    // prefix 를 제외한 뒷부분을 권한 으로 넣어서 사용
    // @PreAuthorize("hasRole('WRITE')") : 쓰기 권한이 있는 유저만 호출 가능하도록 제한
}
