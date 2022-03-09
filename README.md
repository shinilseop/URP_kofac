# 학부생 연구프로그램 (URP)

### Contents

0. [소개](#0-소개)
1. [배경 및 목적](#1-배경-및-목적)
2. [사용 부품](#2-사용-부품)
3. [센싱 디바이스](#3-센싱-디바이스)
4. [조명](#4-조명)
5. [IoT 서버](#5-iot-서버)
6. [구동 App](#6-구동-app)
7. [결과 및 시연 영상](#7-결과-및-시연-영상)

---

### 0. 소개
* 저는 저는 공주대학교 공과대학의 컴퓨터공학부 소프트웨어전공 졸업생 신일섭입니다.
* 본 프로젝트는 한국과학창의재단에서 주관하는 학부생연구프로그램(URP)를 참여하면서 진행했던 프로젝트입니다.

---

### 1. 배경 및 목적
* 연구 배경 및 목적

<p align="center"><img src="/Image/background.png"  width="70%" height="70%"/></p>
 
> - 자연광의 색온도는 하루를 주기로 변화하며 건강에 가장 유익한 빛을 제공<br>
> - 우리가 매일 마주하는 대부분의 인공조명은 고정된 색온도의 빛 환경을 제공하여 건강에 부정적 영향을 미침<br>
> - 일출부터 일몰까지의 변화하는 자연광을 광특성을 측정하기 위한 센싱디바이스 제작과 이를 재현하는 조명을 설계 및 제작<br>
> - 이러한 센싱디바이스와 조명을 제어하기위한 IoT 서버 및 어플리케이션을 구축 및 제작<br>

---

### 2. 사용 부품
* 센싱 디바이스

보드|센서|통신모듈|디퓨저
---|---|---|---
<img src="/Image/sizing/uno.png"  width="100%" height="100%"/>|<img src="/Image/sizing/bh1745.png"  width="100%" height="100%"/>|<img src="/Image/sizing/PHPoC.png"  width="100%" height="100%"/>|<img src="/Image/sizing/difuser.png"  width="100%" height="100%"/>

> - Arduino Uno Board와 BH1745 컬러센서, PHPoC 보드를 이용하여 센서를 제작<br>
> - 너무 강한 태양의 조도를 감쇄시키기 위해 조도의 수광부 위에 디퓨저를 부착

<br>

* 조명

모듈|틀
---|---
<img src="/Image/sizing/light_module.png"  width="100%" height="100%"/>|<img src="/Image/sizing/light_case_model.png"  width="100%" height="100%"/>

> - 변화 가능한 조명을 위해 4CH의 모듈을 설치<br>
> - 3D 프린터를 이용하여 조명 모듈을 부착하기 위한 틀을 제작
    
---

### 3. 센싱 디바이스

*  센서 비교

<p align="center"><img src="/Image/sensor_system.png"  width="70%" height="70%"/></p>

> - 정확도가 높은 센서를 사용하기 위해 3종의 측정 센서를 비교 측정하는 실험을 진행함<br>
> - 기준 장비인 CAS-140CT와 태양을 바라보며 24시간 측정을 진행

<br>

<p align="center"><img src="/Image/McCamy.png"  width="70%" height="70%"/></p>

> - 컬러센서들은 CCT(상관색온도)값을 제공하는 센서도 있으나, 제공하지 않는 센서들도 존재
> - RGB 값을 기반으로 RGB to XYZ to xy to CCT를 하여 상관색온도를 산출하는 McCamy 알고리즘을 이용

<br>

<p align="center"><img src="/Image/sensor_regression.png"  width="70%" height="70%"/></p>

> - 측정 후 기준장비 대비 선형회귀를 통한 센서 보정을 진행<br>
> - 보정은 Scikit-Learn 패키지의 LinearRegression을 사용함<br>
> - 비교 결과, BH1745 센서가 가장 정확도가 높아 해당 센서를 최종적으로 채택

<br>

*  센싱 디바이스

<p align="center"><img src="/Image/sensor_final.png"  width="70%" height="70%"/></p>

> - 내구도 향상을 위해 케이스를 제작하여 최종적으로 센서제작을 마무리

---

### 4. 조명

*  조명 기판

<p align="center"><img src="/Image/light_system.png"  width="70%" height="70%"/></p>

> - 변화하는 자연광을 재현하기위해 4CH의 광원(Warm, Cool)으로 이루어진 조명패널을 제작<br>
> - 조명 제어를 위해 블루투스 통신이 가능한 조명 제어 모듈을 제작

<br>

*  조명 틀

<p align="center"><img src="/Image/light_1.png"  width="70%" height="70%"/></p>

> - 조명 기판과 통신 모듈을 부착할 수 있도록 3D 프린터를 이용하여 조명 틀을 제작<br>
> - 조명 틀 겉부분에 빛을 확산시켜주는 조명 디퓨저를 부착하여 빛을 퍼지도록 제작

<br>

*  시연 영상

<p align="center"><img src="/Image/light_on3.png"  width="70%" height="70%"/></p>

> - 조명을 다른 방식으로 제어했을때의 사진 (왼쪽: 저색온도, 오른쪽: 고색온도)

---

### 5. IoT 서버

<p align="center"><img src="/Image/mobius.png"  width="70%" height="70%"/></p>

> - 센싱되는 데이터를 수집하고 사용자에게 조명의 특성에 맞는 제어지표를 전송하기 위해 서버를 구축<br>
> - 서버는 오픈소스의 개방형 IoT 플랫폼인 Mobius&Cube를 이용하여 구축함<br>
> - 센서는 주기적으로 데이터를 서버로 전송을 시도<br>
> - 사용자는 제어지표를 요청하면 가장 최근의 데이터를 기반으로 해당 색온도와 일치하는 제어지표를 전송받음
---

### 6. 구동 App

<p align="center"><img src="/Image/app1.png"  width="70%" height="70%"/></p>

> - 조명을 제어하기 위한 어플리케이션을 제작<br>
> - 제어할 조명을 선택해서 블루투스로 연결을 하고 제어<br>
> - 가운데 전원버튼은 단순히 on, off 역할을 함<br>
> - Concurrent Status 옆의 검정 아이콘 버튼을 누르면 현재 측정중인 데이터의 제어지표로 지속해서 제어해줌<br>
> - Detailed Setting 의 경우 원하는 조도 및 색온도와 가장 근접한 제어지표를 서버에서 탐색해와서 제어함
---

### 7. 결과 및 시연 영상

*  재현 결과

<p align="center"><img src="/Image/result.png"  width="70%" height="70%"/></p>

> - 하루동안 데이터를 측정하면서 재현해본 결과, 위의 그림처럼 제어가 완료되었음<br>
> - 약 9시경에 측정된 색온도 데이터가 이상데이터로 판정되면서 절기별 대표추세선으로 재현대상이 교체됨

<br>

*  시연 영상

<p align="center"><img src="/Image/light_video.gif"  width="70%" height="70%"/></p>
