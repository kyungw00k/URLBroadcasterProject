/*
 *  Copyright 1995-2004 Daum Communications Corp.
 *   *
 *   * 이 프로그램은 (주)다음커뮤니케이션 공용 소프트웨어 라이센스에 의거하여
 *   * 정해진 권한내에서 사용이 가능하다. 라이센스 원문은 LICENSE 파일이나
 *   * 아래의 URL을 참고하라.
 *   *       http://dna.daumcorp.com/forge/docs/daum-license-1.0.txt
 *
 */

package kyungw00k.URLBroadcaster;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class EMF {
    private static final EntityManagerFactory INSTANCE =
            Persistence.createEntityManagerFactory("transactions-optional");

    private EMF() {
    }

    public static EntityManagerFactory get() {
        return INSTANCE;
    }
}