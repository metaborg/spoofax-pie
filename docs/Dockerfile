FROM python:3.10-alpine

RUN apk upgrade --update-cache -a \
 && apk add --no-cache \
      git \
      git-fast-import \
      openssh \
 && apk add --no-cache --virtual .build gcc musl-dev

COPY mkdocs_requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt \
 && apk del .build gcc musl-dev \
 && rm -rf /tmp/* /root/.cache \
 && find ${PACKAGES} \
      -type f \
      -path "*/__pycache__/*" \
      -exec rm -f {} \;

# Set working directory
WORKDIR /docs
EXPOSE 8000
ENTRYPOINT ["mkdocs"]
CMD ["serve", "--dev-addr=0.0.0.0:8000"]
