-- CONSULTAR ESTADO DEL JOB
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_PROCESAR_SOLICITUDES';

-- CONSULTAR SOLICITUDES
SELECT *
FROM job_solicitudes
ORDER BY id;

-- CONTAR REGISTROS POR ESTADO
SELECT estado, COUNT(*) AS cantidad
FROM job_solicitudes
GROUP BY estado;

-- CONSULTAR BITÁCORA DE LA APLICACIÓN
SELECT *
FROM job_log
ORDER BY log_id DESC;

-- CONSULTAR HISTORIAL ADMINISTRADO POR ORACLE
SELECT
    log_date,
    status,
    actual_start_date,
    run_duration,
    additional_info
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_PROCESAR_SOLICITUDES'
ORDER BY log_date DESC;