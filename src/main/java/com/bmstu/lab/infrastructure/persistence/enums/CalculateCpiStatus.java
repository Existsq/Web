package com.bmstu.lab.infrastructure.persistence.enums;

public enum CalculateCpiStatus {
  DRAFT, // черновик (корзина)
  DELETED, // удалена (логическое удаление)
  FORMED, // сформирована пользователем
  COMPLETED, // завершена модератором
  REJECTED // отклонена модератором
}
